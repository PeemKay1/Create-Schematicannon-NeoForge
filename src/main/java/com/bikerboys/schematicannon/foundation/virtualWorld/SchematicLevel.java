package com.bikerboys.schematicannon.foundation.virtualWorld;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import com.bikerboys.schematicannon.foundation.NBTProcessors;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ExplosionParticleInfo;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.attribute.EnvironmentAttributeSystem;
import net.minecraft.world.clock.ClockManager;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragonPart;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.crafting.RecipeAccess;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.FuelValues;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.ticks.BlackholeTickAccess;
import net.minecraft.world.ticks.LevelTickAccess;

/**
 * In-memory level used to place and inspect structure templates without touching
 * the real world. It is deliberately standalone and contains no Create/Catnip
 * or client-only classes, so the cannon can use it on dedicated servers.
 */
public class SchematicLevel extends Level implements ServerLevelAccessor {
	@Override public Collection<EnderDragonPart> dragonParts() { return Collections.emptyList(); }
	private final Level wrapped;
	private final BlockPos anchor;
	private final Map<BlockPos, BlockState> blocks = new HashMap<>();
	private final Map<BlockPos, BlockEntity> blockEntities = new HashMap<>();
	private final List<BlockEntity> renderedBlockEntities = new ArrayList<>();
	private final List<Entity> entities = new ArrayList<>();
	private final LevelEntityGetter<Entity> entityGetter = new VirtualLevelEntityGetter<>();
	private BoundingBox bounds = new BoundingBox(BlockPos.ZERO);
	public boolean renderMode;

	public SchematicLevel(Level wrapped) {
		this(BlockPos.ZERO, wrapped);
	}

	public SchematicLevel(BlockPos anchor, Level wrapped) {
		super((WritableLevelData) wrapped.getLevelData(), wrapped.dimension(), wrapped.registryAccess(),
			wrapped.dimensionTypeRegistration(), wrapped.isClientSide(), wrapped.isDebug(), 0, 0);
		this.wrapped = wrapped;
		this.anchor = anchor;
	}

	public Set<BlockPos> getAllPositions() {
		return blocks.keySet();
	}

	public List<Entity> getEntityList() {
		return entities;
	}

	public Iterable<BlockEntity> getBlockEntities() {
		return blockEntities.values();
	}

	public Iterable<BlockEntity> getRenderedBlockEntities() {
		return renderedBlockEntities;
	}

	public Map<BlockPos, BlockState> getBlockMap() {
		return blocks;
	}

	public BoundingBox getBounds() {
		return bounds;
	}

	public void setBounds(BoundingBox bounds) {
		this.bounds = bounds;
	}

	@Override
	public boolean addFreshEntity(Entity entity) {
		if (entity instanceof ItemFrame frame)
			frame.setItem(NBTProcessors.withUnsafeNBTDiscarded(frame.getItem()));
		if (entity instanceof ArmorStand stand)
			for (EquipmentSlot slot : EquipmentSlot.VALUES)
				stand.setItemSlot(slot, NBTProcessors.withUnsafeNBTDiscarded(stand.getItemBySlot(slot)));
		return entities.add(entity);
	}

	@Override
	public BlockState getBlockState(BlockPos globalPos) {
		BlockPos relative = globalPos.subtract(anchor);
		if (relative.getY() == bounds.minY() - 1 && !renderMode)
			return Blocks.DIRT.defaultBlockState();
		BlockState state = bounds.isInside(relative) ? blocks.get(relative) : null;
		if (state == null)
			return Blocks.AIR.defaultBlockState();
		if (state.getBlock() instanceof AbstractFurnaceBlock && state.hasProperty(BlockStateProperties.LIT))
			return state.setValue(BlockStateProperties.LIT, false);
		return state;
	}

	@Override
	public FluidState getFluidState(BlockPos pos) {
		return getBlockState(pos).getFluidState();
	}

	@Override
	@Nullable
	public BlockEntity getBlockEntity(BlockPos globalPos) {
		if (isOutsideBuildHeight(globalPos))
			return null;
		BlockEntity existing = blockEntities.get(globalPos);
		if (existing != null)
			return existing;
		BlockState state = getBlockState(globalPos);
		if (!(state.getBlock() instanceof EntityBlock entityBlock))
			return null;
		try {
			BlockEntity created = entityBlock.newBlockEntity(globalPos, state);
			if (created != null) {
				created.setLevel(this);
				blockEntities.put(globalPos.immutable(), created);
				renderedBlockEntities.add(created);
			}
			return created;
		} catch (RuntimeException ignored) {
			return null;
		}
	}

	@Override
	public boolean setBlock(BlockPos globalPos, BlockState state, int flags, int recursionLeft) {
		BlockPos relative = globalPos.immutable().subtract(anchor);
		bounds = encapsulate(bounds, relative);
		blocks.put(relative, state);
		BlockEntity existing = blockEntities.get(globalPos);
		if (existing != null && !existing.getType().isValid(state)) {
			blockEntities.remove(globalPos);
			renderedBlockEntities.remove(existing);
		}
		getBlockEntity(globalPos);
		return true;
	}

	@Override
	public void setBlockEntity(BlockEntity blockEntity) {
		blockEntity.setLevel(this);
		blockEntities.put(blockEntity.getBlockPos().immutable(), blockEntity);
		if (!renderedBlockEntities.contains(blockEntity))
			renderedBlockEntities.add(blockEntity);
	}

	@Override
	public void removeBlockEntity(BlockPos pos) {
		BlockEntity removed = blockEntities.remove(pos);
		if (removed != null)
			renderedBlockEntities.remove(removed);
	}

	@Override
	public boolean removeBlock(BlockPos pos, boolean moving) {
		return setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
	}

	@Override
	public int getBrightness(LightLayer layer, BlockPos pos) {
		return 15;
	}

	@Override
	public int getSkyDarken() {
		return 0;
	}

	public float getShade(Direction direction, boolean shade) {
		return 1;
	}

	@Override
	public Holder<Biome> getBiome(BlockPos pos) {
		return registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.PLAINS);
	}

	@Override
	public Holder<Biome> getUncachedNoiseBiome(int x, int y, int z) {
		return registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.PLAINS);
	}

	@Override
	public int getHeight() {
		return wrapped.getHeight();
	}

	@Override
	public int getMinY() {
		return wrapped.getMinY();
	}

	@Override
	public int getSeaLevel() {
		return wrapped.getSeaLevel();
	}

	@Override
	public WorldBorder getWorldBorder() {
		return wrapped.getWorldBorder();
	}

	@Override
	public boolean isLoaded(BlockPos pos) {
		return true;
	}

	@Override
	public ChunkSource getChunkSource() {
		return wrapped.getChunkSource();
	}

	@Override
	public LevelLightEngine getLightEngine() {
		return wrapped.getLightEngine();
	}

	@Override
	public LevelTickAccess<Block> getBlockTicks() {
		return BlackholeTickAccess.emptyLevelList();
	}

	@Override
	public LevelTickAccess<Fluid> getFluidTicks() {
		return BlackholeTickAccess.emptyLevelList();
	}

	@Override
	protected LevelEntityGetter<Entity> getEntities() {
		return entityGetter;
	}

	@Override public void sendBlockUpdated(BlockPos pos, BlockState oldState, BlockState newState, int flags) {}
	@Override public void levelEvent(@Nullable Entity entity, int type, BlockPos pos, int data) {}
	@Override public void gameEvent(Holder<GameEvent> event, Vec3 pos, GameEvent.Context context) {}
	@Override public void playSeededSound(Entity player, double x, double y, double z, Holder<SoundEvent> sound,
										 SoundSource source, float volume, float pitch, long seed) {}
	@Override public void playSeededSound(Entity player, Entity entity, Holder<SoundEvent> sound,
										 SoundSource source, float volume, float pitch, long seed) {}
	@Override public void explode(Entity entity, DamageSource damageSource, ExplosionDamageCalculator calculator,
								  double x, double y, double z, float radius, boolean fire,
								  ExplosionInteraction interaction, ParticleOptions small, ParticleOptions large,
								  WeightedList<ExplosionParticleInfo> particles, Holder<SoundEvent> sound) {}
	@Override public String gatherChunkSourceStats() { return "SchematicLevel"; }
	@Override public void setRespawnData(LevelData.RespawnData data) {}
	@Override public LevelData.RespawnData getRespawnData() { return wrapped.getRespawnData(); }
	@Override public Entity getEntity(int id) { return null; }
	@Override public TickRateManager tickRateManager() { return wrapped.tickRateManager(); }
	@Override public MapItemSavedData getMapData(MapId id) { return wrapped.getMapData(id); }
	@Override public void destroyBlockProgress(int breakerId, BlockPos pos, int progress) {}
	@Override public Scoreboard getScoreboard() { return wrapped.getScoreboard(); }
	@Override public RecipeAccess recipeAccess() { return wrapped.recipeAccess(); }
	@Override public ClockManager clockManager() { return wrapped.clockManager(); }
	@Override public EnvironmentAttributeSystem environmentAttributes() { return wrapped.environmentAttributes(); }
	@Override public PotionBrewing potionBrewing() { return wrapped.potionBrewing(); }
	@Override public FuelValues fuelValues() { return wrapped.fuelValues(); }
	@Override public FeatureFlagSet enabledFeatures() { return wrapped.enabledFeatures(); }
	@Override public List<? extends Player> players() { return Collections.emptyList(); }

	@Override
	public ServerLevel getLevel() {
		if (wrapped instanceof ServerLevel serverLevel)
			return serverLevel;
		throw new IllegalStateException("A client schematic level has no ServerLevel");
	}

	@Override
	public DifficultyInstance getCurrentDifficultyAt(BlockPos pos) {
		if (wrapped instanceof ServerLevel serverLevel)
			return serverLevel.getCurrentDifficultyAt(pos);
		return new DifficultyInstance(wrapped.getLevelData().getDifficulty(),
			wrapped.getLevelData().getGameTime(), 0, 0);
	}

	private static BoundingBox encapsulate(BoundingBox box, BlockPos pos) {
		return new BoundingBox(Math.min(box.minX(), pos.getX()), Math.min(box.minY(), pos.getY()),
			Math.min(box.minZ(), pos.getZ()), Math.max(box.maxX(), pos.getX()),
			Math.max(box.maxY(), pos.getY()), Math.max(box.maxZ(), pos.getZ()));
	}
}

package com.bikerboys.schematicannon.content.schematics.cannon;

import com.bikerboys.schematicannon.foundation.utility.BlockHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityProcessor;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public abstract class LaunchedItem {

	public int totalTicks;
	public int ticksRemaining;
	public BlockPos target;
	public ItemStack stack;

	private LaunchedItem(BlockPos start, BlockPos target, ItemStack stack) {
		this(target, stack, ticksForDistance(start, target), ticksForDistance(start, target));
	}

	private static int ticksForDistance(BlockPos start, BlockPos target) {
		return (int) (Math.max(10, Math.sqrt(Math.sqrt(target.distSqr(start))) * 4f));
	}

	LaunchedItem() {}

	private LaunchedItem(BlockPos target, ItemStack stack, int ticksLeft, int total) {
		this.target = target;
		this.stack = stack;
		this.totalTicks = total;
		this.ticksRemaining = ticksLeft;
	}

	public boolean update(Level world) {
		if (ticksRemaining > 0) {
			ticksRemaining--;
			return false;
		}
		if (world.isClientSide())
			return false;

		place(world);
		return true;
	}

	public void write(ValueOutput output) {
		output.putInt("TotalTicks", totalTicks);
		output.putInt("TicksLeft", ticksRemaining);
		output.store("Stack", ItemStack.CODEC, stack);
		output.store("Target", BlockPos.CODEC, target);
	}

	public static LaunchedItem from(ValueInput input) {
		LaunchedItem launched = input.read("BlockState", BlockState.CODEC).isPresent()
			? new LaunchedItem.ForBlockState()
			: new LaunchedItem.ForEntity();

		launched.read(input);
		return launched;
	}

	abstract void place(Level world);

	void read(ValueInput input) {
		target = input.read("Target", BlockPos.CODEC).orElse(BlockPos.ZERO);
		ticksRemaining = input.getIntOr("TicksLeft", 0);
		totalTicks = input.getIntOr("TotalTicks", ticksRemaining);
		stack = input.read("Stack", ItemStack.CODEC).orElse(ItemStack.EMPTY);
	}

	/**
	 * Reads the 1.20.1 representation so in-flight cannon shots survive a world upgrade.
	 */
	public static LaunchedItem fromLegacyNBT(CompoundTag nbt, HolderLookup.Provider registries) {
		LaunchedItem launched = nbt.contains("BlockState") ? new ForBlockState() : new ForEntity();
		launched.target = nbt.getCompound("Target")
			.map(target -> new BlockPos(target.getIntOr("X", 0), target.getIntOr("Y", 0), target.getIntOr("Z", 0)))
			.orElse(BlockPos.ZERO);
		launched.ticksRemaining = nbt.getIntOr("TicksLeft", 0);
		launched.totalTicks = nbt.getIntOr("TotalTicks", launched.ticksRemaining);
		launched.stack = nbt.getCompound("Stack")
			.flatMap(stack -> ItemStack.CODEC
				.parse(registries.createSerializationContext(NbtOps.INSTANCE), stack)
				.result())
			.orElse(ItemStack.EMPTY);

		if (launched instanceof ForBlockState block) {
			block.state = nbt.getCompound("BlockState")
				.map(state -> NbtUtils.readBlockState(registries.lookupOrThrow(Registries.BLOCK), state))
				.orElse(net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
			block.data = nbt.getCompound("Data").orElse(null);
		} else if (launched instanceof ForEntity entity) {
			entity.deferredTag = nbt.getCompound("Entity").orElse(null);
		}
		return launched;
	}

	public static class ForBlockState extends LaunchedItem {
		public BlockState state;
		public CompoundTag data;

		ForBlockState() {}

		public ForBlockState(BlockPos start, BlockPos target, ItemStack stack, BlockState state, CompoundTag data) {
			super(start, target, stack);
			this.state = state;
			this.data = data;
		}

		@Override
		public void write(ValueOutput output) {
			super.write(output);
			output.store("BlockState", BlockState.CODEC, state);
			if (data != null) {
				CompoundTag cleanedData = data.copy();
				cleanedData.remove("x");
				cleanedData.remove("y");
				cleanedData.remove("z");
				cleanedData.remove("id");
				output.store("Data", CompoundTag.CODEC, cleanedData);
			}
		}

		@Override
		void read(ValueInput input) {
			super.read(input);
			state = input.read("BlockState", BlockState.CODEC)
				.orElse(net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
			data = input.read("Data", CompoundTag.CODEC).orElse(null);
		}

		@Override
		void place(Level world) {
			BlockHelper.placeSchematicBlock(world, state, target, stack, data);
		}

	}



	public static class ForEntity extends LaunchedItem {
		public Entity entity;
		private CompoundTag deferredTag;

		ForEntity() {}

		public ForEntity(BlockPos start, BlockPos target, ItemStack stack, Entity entity) {
			super(start, target, stack);
			this.entity = entity;
		}

		@Override
		public boolean update(Level world) {
			if (deferredTag != null && entity == null) {
				try {
					ValueInput input = TagValueInput.create(ProblemReporter.DISCARDING, world.registryAccess(), deferredTag);
					entity = EntityType.loadEntityRecursive(input, world, EntitySpawnReason.LOAD, EntityProcessor.NOP);
					if (entity == null)
						return true;
				} catch (Exception ignored) {
					return true;
				}
				deferredTag = null;
			}
			return super.update(world);
		}

		@Override
		public void write(ValueOutput output) {
			super.write(output);
			if (entity != null)
				entity.save(output.child("Entity"));
			else if (deferredTag != null)
				output.store("Entity", CompoundTag.CODEC, deferredTag);
		}

		@Override
		void read(ValueInput input) {
			super.read(input);
			deferredTag = input.read("Entity", CompoundTag.CODEC).orElse(null);
		}

		@Override
		void place(Level world) {
			if (entity != null)
				world.addFreshEntity(entity);
		}

	}

}

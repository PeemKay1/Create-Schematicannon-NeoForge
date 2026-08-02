package com.bikerboys.schematicannon.content.schematics.cannon;

import com.bikerboys.schematicannon.AllBlockEntityTypes;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nullable;

import com.bikerboys.schematicannon.AllBlocks;
import com.bikerboys.schematicannon.AllDataComponents;
import com.bikerboys.schematicannon.AllItems;
import com.bikerboys.schematicannon.AllSoundEvents;
import com.bikerboys.schematicannon.content.schematics.SchematicPrinter;
import com.bikerboys.schematicannon.content.schematics.SchematicItem;
import com.bikerboys.schematicannon.content.schematics.requirement.ItemRequirement;
import com.bikerboys.schematicannon.content.schematics.requirement.ItemRequirement.ItemUseType;
import com.bikerboys.schematicannon.foundation.blockEntity.SmartBlockEntity;
import com.bikerboys.schematicannon.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.bikerboys.schematicannon.foundation.item.ItemHelper;
import com.bikerboys.schematicannon.foundation.item.ItemHelper.ExtractionCountMode;
import com.bikerboys.schematicannon.foundation.utility.BlockHelper;
import com.bikerboys.schematicannon.foundation.utility.CreateLang;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

public class SchematicannonBlockEntity extends SmartBlockEntity implements MenuProvider {

	public static final int NEIGHBOUR_CHECKING = 100;
	public static final int MAX_ANCHOR_DISTANCE = 256;

	// Inventory
	public SchematicannonInventory inventory;

	public boolean sendUpdate;
	// Sync
	public boolean dontUpdateChecklist;
	public int neighbourCheckCooldown;

	// Printer
	public SchematicPrinter printer;
	public ItemStack missingItem;
	public boolean positionNotLoaded;
	public boolean hasCreativeCrate;
	private int printerCooldown;
	private int skipsLeft;
	private boolean blockSkipped;

	public BlockPos previousTarget;
	public LinkedHashSet<IItemHandler> attachedInventories;
	public List<LaunchedItem> flyingBlocks;
	private ListTag pendingLegacyFlyingBlocks;
	public MaterialChecklist checklist;

	// Gui information
	public int remainingFuel;
	public float bookPrintingProgress;
	public float schematicProgress;
	public String statusMsg;
	public State state;
	public int blocksPlaced;
	public int blocksToPlace;

	// Settings
	public int replaceMode;
	public boolean skipMissing;
	public boolean replaceBlockEntities;

	// Render
	public boolean firstRenderTick;
	public float defaultYaw;

	public SchematicannonBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		setLazyTickRate(30);
		attachedInventories = new LinkedHashSet<>();
		flyingBlocks = new LinkedList<>();
		inventory = new SchematicannonInventory(this);
		statusMsg = "idle";
		this.state = State.STOPPED;
		replaceMode = 2;
		checklist = new MaterialChecklist();
		printer = new SchematicPrinter();
	}

	public void findInventories() {
		hasCreativeCrate = false;
		attachedInventories.clear();
		for (Direction facing : Direction.values()) {

			if (!level.isLoaded(worldPosition.relative(facing)))
				continue;



			ResourceHandler<ItemResource> capability = level.getCapability(Capabilities.Item.BLOCK,
				worldPosition.relative(facing), facing.getOpposite());
			if (capability != null)
				attachedInventories.add(IItemHandler.of(capability));
		}
	}

	@Override
	protected void read(CompoundTag compound, boolean clientPacket) {
		// Gui information
		statusMsg = compound.getStringOr("Status", "idle");
		schematicProgress = compound.getFloatOr("Progress", 0);
		bookPrintingProgress = compound.getFloatOr("PaperProgress", 0);
		remainingFuel = compound.getIntOr("RemainingFuel", 0);
		String stateString = compound.getStringOr("State", "");
		state = stateString.isEmpty() ? State.STOPPED : State.valueOf(stateString);
		blocksPlaced = compound.getIntOr("AmountPlaced", 0);
		blocksToPlace = compound.getIntOr("AmountToPlace", 0);

		// Settings
		CompoundTag options = compound.getCompoundOrEmpty("Options");
		replaceMode = options.getIntOr("ReplaceMode", 0);
		skipMissing = options.getBooleanOr("SkipMissing", false);
		replaceBlockEntities = options.getBooleanOr("ReplaceTileEntities", false);

		// Printer & Flying Blocks
		if (compound.contains("Printer"))
			printer.fromTag(compound.getCompoundOrEmpty("Printer"), clientPacket);
		if (compound.contains("FlyingBlocks")) {
			ListTag blocks = compound.getListOrEmpty("FlyingBlocks");
			if (level != null)
				readFlyingBlocks(blocks, level.registryAccess());
			else
				pendingLegacyFlyingBlocks = blocks.copy();
		}

		defaultYaw = compound.getFloatOr("DefaultYaw", 0);

		super.read(compound, clientPacket);
	}

	protected void readFlyingBlocks(ListTag tagBlocks, net.minecraft.core.HolderLookup.Provider registries) {
		List<LaunchedItem> launchedItems = new LinkedList<>();
		for (int i = 0; i < tagBlocks.size(); i++) {
			CompoundTag tag = tagBlocks.getCompoundOrEmpty(i);
			boolean legacy = tag.get("Target") instanceof CompoundTag;
			if (legacy)
				launchedItems.add(LaunchedItem.fromLegacyNBT(tag, registries));
			else
				launchedItems.add(LaunchedItem.from(
					TagValueInput.create(ProblemReporter.DISCARDING, registries, tag)));
		}
		readFlyingBlocks(launchedItems);
	}

	public SchematicannonBlockEntity(BlockPos pos, BlockState state) {
		this(AllBlockEntityTypes.SCHEMATICANNON.get(), pos, state);
	}

	protected void readFlyingBlocks(Iterable<LaunchedItem> launchedItems) {
		boolean empty = !launchedItems.iterator().hasNext();
		if (empty)
			flyingBlocks.clear();

		boolean pastDead = false;
		int i = 0;
		for (LaunchedItem launched : launchedItems) {
			int index = i++;
			BlockPos readBlockPos = launched.target;

			// Always write to Server block entity
			if (level == null || !level.isClientSide()) {
				flyingBlocks.add(launched);
				continue;
			}

			// Delete all Client side blocks that are now missing on the server
			while (!pastDead && !flyingBlocks.isEmpty() && !flyingBlocks.get(0).target.equals(readBlockPos)) {
				flyingBlocks.remove(0);
			}

			pastDead = true;

			// Add new server side blocks
			if (index >= flyingBlocks.size()) {
				flyingBlocks.add(launched);
				continue;
			}

			// Don't do anything with existing
		}
	}

	@Override
	public void write(CompoundTag compound, boolean clientPacket) {
		if (!clientPacket) {
			if (state == State.RUNNING) {
				compound.putBoolean("Running", true);
			}
		}

		// Gui information
		compound.putFloat("Progress", schematicProgress);
		compound.putFloat("PaperProgress", bookPrintingProgress);
		compound.putInt("RemainingFuel", remainingFuel);
		compound.putString("Status", statusMsg);
		compound.putString("State", state.name());
		compound.putInt("AmountPlaced", blocksPlaced);
		compound.putInt("AmountToPlace", blocksToPlace);

		// Settings
		CompoundTag options = new CompoundTag();
		options.putInt("ReplaceMode", replaceMode);
		options.putBoolean("SkipMissing", skipMissing);
		options.putBoolean("ReplaceTileEntities", replaceBlockEntities);
		compound.put("Options", options);

		// Printer & Flying Blocks
		CompoundTag printerData = new CompoundTag();
		printer.write(printerData);
		compound.put("Printer", printerData);

		if (clientPacket && level != null) {
			ListTag tagFlyingBlocks = new ListTag();
			for (LaunchedItem b : flyingBlocks) {
				TagValueOutput output =
					TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
				b.write(output);
				tagFlyingBlocks.add(output.buildResult());
			}
			compound.put("FlyingBlocks", tagFlyingBlocks);
		}

		compound.putFloat("DefaultYaw", defaultYaw);

		super.write(compound, clientPacket);
	}

	@Override
	protected void readValue(ValueInput input) {
		input.readChild("Inventory", inventory);
		missingItem = input.read("MissingItem", ItemStack.CODEC).orElse(null);
		input.childrenList("FlyingBlocks")
			.ifPresent(blocks -> readFlyingBlocks(blocks.stream()
				.map(LaunchedItem::from)
				.toList()));
		if (pendingLegacyFlyingBlocks != null) {
			readFlyingBlocks(pendingLegacyFlyingBlocks, input.lookup());
			pendingLegacyFlyingBlocks = null;
		}
	}

	@Override
	protected void writeValue(ValueOutput output) {
		output.putChild("Inventory", inventory);
		if (missingItem != null)
			output.store("MissingItem", ItemStack.CODEC, missingItem);
		else
			output.discard("MissingItem");
		ValueOutput.ValueOutputList blocks = output.childrenList("FlyingBlocks");
		for (LaunchedItem launched : flyingBlocks)
			launched.write(blocks.addChild());
	}

	@Override
	public void tick() {
		super.tick();

		if (state != State.STOPPED && neighbourCheckCooldown-- <= 0) {
			neighbourCheckCooldown = NEIGHBOUR_CHECKING;
			findInventories();
		}

		firstRenderTick = true;
		previousTarget = printer.getCurrentTarget();
		tickFlyingBlocks();

		if (level.isClientSide())
			return;

		// Update Fuel and Paper
		tickPaperPrinter();
		refillFuelIfPossible();

		// Update Printer
		skipsLeft = 1000;
		blockSkipped = true;

		while (blockSkipped && skipsLeft-- > 0)
			tickPrinter();

		schematicProgress = 0;
		if (blocksToPlace > 0)
			schematicProgress = (float) blocksPlaced / blocksToPlace;

		// Update Client block entity
		if (sendUpdate) {
			sendUpdate = false;
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 6);
		}
	}



	protected void tickPrinter() {
		ItemStack blueprint = inventory.getStackInSlot(0);
		blockSkipped = false;

		if (blueprint.isEmpty() && !statusMsg.equals("idle") && inventory.getStackInSlot(1)
			.isEmpty()) {
			state = State.STOPPED;
			statusMsg = "idle";
			sendUpdate = true;
			return;
		}

		// Skip if not Active
		if (state == State.STOPPED) {
			if (printer.isLoaded())
				resetPrinter();
			return;
		}

		if (state == State.PAUSED && !positionNotLoaded && missingItem == null && remainingFuel > 0)
			return;

		// Initialize Printer
		if (!printer.isLoaded()) {
			initializePrinter(blueprint);
			return;
		}

		// Cooldown from last shot
		if (printerCooldown > 0) {
			printerCooldown--;
			return;
		}

		// Check Fuel
		if (remainingFuel <= 0 && !hasCreativeCrate) {
			refillFuelIfPossible();
			if (remainingFuel <= 0) {
				state = State.PAUSED;
				statusMsg = "noGunpowder";
				sendUpdate = true;
				return;
			}
		}

		if (hasCreativeCrate) {
			remainingFuel = 0;
			if (missingItem != null) {
				missingItem = null;
				state = State.RUNNING;
			}
		}

		// Update Target
		if (missingItem == null && !positionNotLoaded) {
			if (!printer.advanceCurrentPos()) {
				finishedPrinting();
				return;
			}
			sendUpdate = true;
		}

		// Check block
		if (!getLevel().isLoaded(printer.getCurrentTarget())) {
			positionNotLoaded = true;
			statusMsg = "targetNotLoaded";
			state = State.PAUSED;
			return;
		} else {
			if (positionNotLoaded) {
				positionNotLoaded = false;
				state = State.RUNNING;
			}
		}

		// Get item requirement
		ItemRequirement requirement = printer.getCurrentRequirement();
		if (requirement.isInvalid() || !printer.shouldPlaceCurrent(level, this::shouldPlace)) {
			sendUpdate = !statusMsg.equals("searching");
			statusMsg = "searching";
			blockSkipped = true;
			return;
		}

		// Find item
		List<ItemRequirement.StackRequirement> requiredItems = requirement.getRequiredItems();
		if (!requirement.isEmpty()) {
			for (ItemRequirement.StackRequirement required : requiredItems) {
				if (!grabItemsFromAttachedInventories(required, true)) {
					if (skipMissing) {
						statusMsg = "skipping";
						blockSkipped = true;
						if (missingItem != null) {
							missingItem = null;
							state = State.RUNNING;
						}
						return;
					}

					missingItem = required.stack;
					state = State.PAUSED;
					statusMsg = "missingBlock";
					return;
				}
			}

			for (ItemRequirement.StackRequirement required : requiredItems)
				grabItemsFromAttachedInventories(required, false);
		}

		// Success
		state = State.RUNNING;
		ItemStack icon = requirement.isEmpty() || requiredItems.isEmpty() ? ItemStack.EMPTY : requiredItems.get(0).stack;
		printer.handleCurrentTarget((target, blockState, blockEntity) -> {
			// Launch block
			statusMsg = blockState.getBlock() != Blocks.AIR ? "placing" : "clearing";
			launchBlockOrBelt(target, icon, blockState, blockEntity);
		}, (target, entity) -> {
			// Launch entity
			statusMsg = "placing";
			launchEntity(target, icon, entity);
		});

		printerCooldown = 10;
		remainingFuel -= 1;
		sendUpdate = true;
		missingItem = null;
	}

	public int getShotsPerGunpowder() {
		return hasCreativeCrate ? 0 : 400;
	}

	protected void initializePrinter(ItemStack blueprint) {
		boolean startAfterLoading = state == State.RUNNING;
		if (!blueprint.has(AllDataComponents.SCHEMATIC_FILE)) {
			state = State.STOPPED;
			statusMsg = "schematicInvalid";
			sendUpdate = true;
			return;
		}

		if (!blueprint.getOrDefault(AllDataComponents.SCHEMATIC_DEPLOYED, false)
			|| blueprint.getOrDefault(AllDataComponents.SCHEMATIC_PLACEMENT_VERSION, 0)
				< SchematicItem.PLACEMENT_FORMAT_VERSION) {
			state = State.STOPPED;
			statusMsg = "schematicNotPlaced";
			sendUpdate = true;
			return;
		}

		// Load blocks into reader
		printer.loadSchematic(blueprint, level, true);

		if (printer.isErrored()) {
			state = State.STOPPED;
			statusMsg = "schematicErrored";
			inventory.setStackInSlot(0, ItemStack.EMPTY);
			inventory.setStackInSlot(1, new ItemStack(AllItems.EMPTY_SCHEMATIC.get()));
			printer.resetSchematic();
			sendUpdate = true;
			return;
		}

		if (printer.isWorldEmpty()) {
			state = State.STOPPED;
			statusMsg = "schematicExpired";
			inventory.setStackInSlot(0, ItemStack.EMPTY);
			inventory.setStackInSlot(1, new ItemStack(AllItems.EMPTY_SCHEMATIC.get()));
			printer.resetSchematic();
			sendUpdate = true;
			return;
		}

		if (!printer.getAnchor()
			.closerThan(getBlockPos(), MAX_ANCHOR_DISTANCE)) {
			state = State.STOPPED;
			statusMsg = "targetOutsideRange";
			printer.resetSchematic();
			sendUpdate = true;
			return;
		}

		state = startAfterLoading ? State.RUNNING : State.PAUSED;
		statusMsg = startAfterLoading ? "running" : "ready";
		updateChecklist();
		sendUpdate = true;
		blocksToPlace += blocksPlaced;
	}

	protected ItemStack getItemForBlock(BlockState blockState) {
		Item item = BlockItem.BY_BLOCK.getOrDefault(blockState.getBlock(), Items.AIR);
		return item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item);
	}

	protected boolean grabItemsFromAttachedInventories(ItemRequirement.StackRequirement required, boolean simulate) {
		if (hasCreativeCrate)
			return true;

		ItemUseType usage = required.usage;

		// Find and apply damage
		if (usage == ItemUseType.DAMAGE) {
			for (IItemHandler itemHandler : attachedInventories) {
				for (int slot = 0; slot < itemHandler.getSlots(); slot++) {
					ItemStack extractItem = itemHandler.extractItem(slot, 1, true);
					if (!required.matches(extractItem))
						continue;
					if (!extractItem.isDamageableItem())
						continue;

					if (!simulate) {
						ItemStack stack = itemHandler.extractItem(slot, 1, false);
						stack.setDamageValue(stack.getDamageValue() + 1);
						if (stack.getDamageValue() <= stack.getMaxDamage()) {
							if (itemHandler.getStackInSlot(slot)
								.isEmpty())
								itemHandler.insertItem(slot, stack, false);
							else
								ItemHandlerHelper.insertItem(itemHandler, stack, false);
						}
					}

					return true;
				}
			}

			return false;
		}

		// Find and remove
		boolean success = false;
		int amountFound = 0;
		for (IItemHandler itemHandler : attachedInventories) {
			amountFound += ItemHelper
				.extract(itemHandler, required::matches, ExtractionCountMode.UPTO,
					required.stack.getCount(), true)
				.getCount();

			if (amountFound < required.stack.getCount())
				continue;

			success = true;
			break;
		}

		if (!simulate && success) {
			amountFound = 0;
			for (IItemHandler itemHandler : attachedInventories) {
				amountFound += ItemHelper
					.extract(itemHandler, required::matches, ExtractionCountMode.UPTO,
						required.stack.getCount(), false)
					.getCount();
				if (amountFound < required.stack.getCount())
					continue;
				break;
			}
		}

		return success;
	}

	public void finishedPrinting() {
		if (replaceMode == ConfigureSchematicannonPacket.Option.REPLACE_EMPTY.ordinal())
			printer.sendBlockUpdates(level);
		inventory.setStackInSlot(0, ItemStack.EMPTY);
		inventory.setStackInSlot(1, new ItemStack(AllItems.EMPTY_SCHEMATIC.get(), inventory.getStackInSlot(1)
			.getCount() + 1));
		state = State.STOPPED;
		statusMsg = "finished";
		resetPrinter();
		AllSoundEvents.SCHEMATICANNON_FINISH.playOnServer(level, worldPosition);
		sendUpdate = true;
	}

	protected void resetPrinter() {
		printer.resetSchematic();
		missingItem = null;
		sendUpdate = true;
		schematicProgress = 0;
		blocksPlaced = 0;
		blocksToPlace = 0;
	}

	protected boolean shouldPlace(BlockPos pos, BlockState state, BlockEntity be, BlockState toReplace,
		BlockState toReplaceOther, boolean isNormalCube) {
		if (pos.closerThan(getBlockPos(), 2f))
			return false;
		if (!replaceBlockEntities
			&& (toReplace.hasBlockEntity() || (toReplaceOther != null && toReplaceOther.hasBlockEntity())))
			return false;

		if (shouldIgnoreBlockState(state, be))
			return false;

		boolean placingAir = state.isAir();

		if (replaceMode == 3)
			return true;
		if (replaceMode == 2 && !placingAir)
			return true;
		if (replaceMode == 1 && (isNormalCube || (!toReplace.isRedstoneConductor(level, pos)
			&& (toReplaceOther == null || !toReplaceOther.isRedstoneConductor(level, pos)))) && !placingAir)
			return true;
		return replaceMode == 0 && !toReplace.isRedstoneConductor(level, pos)
			&& (toReplaceOther == null || !toReplaceOther.isRedstoneConductor(level, pos)) && !placingAir;
	}

	protected boolean shouldIgnoreBlockState(BlockState state, BlockEntity be) {
		// Block doesn't have a mapping (Water, lava, etc)
		if (state.getBlock() == Blocks.STRUCTURE_VOID)
			return true;

		ItemRequirement requirement = ItemRequirement.of(state, be);
		if (requirement.isEmpty())
			return false;
		if (requirement.isInvalid())
			return false;

		// Block doesn't need to be placed twice (Doors, beds, double plants)
		if (state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)
			&& state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER)
			return true;
		if (state.hasProperty(BlockStateProperties.BED_PART)
			&& state.getValue(BlockStateProperties.BED_PART) == BedPart.HEAD)
			return true;
		return state.getBlock() instanceof PistonHeadBlock;
	}

	protected void tickFlyingBlocks() {
		List<LaunchedItem> toRemove = new LinkedList<>();
		for (LaunchedItem b : flyingBlocks)
			if (b.update(level))
				toRemove.add(b);
		flyingBlocks.removeAll(toRemove);
	}

	protected void refillFuelIfPossible() {
		if (hasCreativeCrate)
			return;
		if (remainingFuel > getShotsPerGunpowder()) {
			remainingFuel = getShotsPerGunpowder();
			sendUpdate = true;
			return;
		}

		if (remainingFuel > 0)
			return;

		if (!inventory.getStackInSlot(4)
			.isEmpty())
			inventory.getStackInSlot(4)
				.shrink(1);
		else {
			boolean externalGunpowderFound = false;
			for (IItemHandler itemHandler : attachedInventories) {
				if (ItemHelper.extract(itemHandler, stack -> inventory.isItemValid(4, stack), 1, false)
					.isEmpty())
					continue;
				externalGunpowderFound = true;
				break;
			}
			if (!externalGunpowderFound)
				return;
		}

		remainingFuel += getShotsPerGunpowder();
		if (statusMsg.equals("noGunpowder")) {
			state = State.RUNNING;
			statusMsg = "running";
		}
		sendUpdate = true;
	}

	protected void tickPaperPrinter() {
		int BookInput = 2;
		int BookOutput = 3;

		ItemStack blueprint = inventory.getStackInSlot(0);
		ItemStack paper = inventory.extractItem(BookInput, 1, true);
		boolean outputFull = inventory.getStackInSlot(BookOutput)
			.getCount() == inventory.getSlotLimit(BookOutput);

		if (printer.isErrored())
			return;

		if (!printer.isLoaded()) {
			if (!blueprint.isEmpty())
				initializePrinter(blueprint);
			return;
		}

		if (paper.isEmpty() || outputFull) {
			if (bookPrintingProgress != 0)
				sendUpdate = true;
			bookPrintingProgress = 0;
			dontUpdateChecklist = false;
			return;
		}

		if (bookPrintingProgress >= 1) {
			bookPrintingProgress = 0;

			if (!dontUpdateChecklist)
				updateChecklist();

			dontUpdateChecklist = true;
			ItemStack extractItem = inventory.extractItem(BookInput, 1, false);
			ItemStack stack = extractItem.is(AllBlocks.CLIPBOARD_ITEM.get()) ? checklist.createWrittenClipboard()
				: checklist.createWrittenBook();
			stack.setCount(inventory.getStackInSlot(BookOutput)
				.getCount() + 1);
			inventory.setStackInSlot(BookOutput, stack);
			sendUpdate = true;
			return;
		}

		bookPrintingProgress += 0.05f;
		sendUpdate = true;
	}



	protected void launchBlockOrBelt(BlockPos target, ItemStack icon, BlockState blockState, BlockEntity blockEntity) {
		CompoundTag data = BlockHelper.prepareBlockEntityData(blockState, blockEntity);
		launchBlock(target, icon, blockState, data);
	}



	protected void launchBlock(BlockPos target, ItemStack stack, BlockState state, @Nullable CompoundTag data) {
		if (!state.isAir())
			blocksPlaced++;
		flyingBlocks.add(new LaunchedItem.ForBlockState(this.getBlockPos(), target, stack, state, data));
		playFiringSound();
	}

	protected void launchEntity(BlockPos target, ItemStack stack, Entity entity) {
		blocksPlaced++;
		flyingBlocks.add(new LaunchedItem.ForEntity(this.getBlockPos(), target, stack, entity));
		playFiringSound();
	}

	public void playFiringSound() {
		AllSoundEvents.SCHEMATICANNON_LAUNCH_BLOCK.playOnServer(level, worldPosition);
	}

	public void sendToMenu(FriendlyByteBuf buffer) {
		buffer.writeBlockPos(getBlockPos());
		buffer.writeNbt(writeClient(new CompoundTag()));
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return SchematicannonMenu.create(id, inv, this);
	}

	@Override
	public Component getDisplayName() {
		return CreateLang.translateDirect("gui.schematicannon.title");
	}

	public void updateChecklist() {
		checklist.required.clear();
		checklist.damageRequired.clear();
		checklist.blocksNotLoaded = false;

		if (printer.isLoaded() && !printer.isErrored()) {
			blocksToPlace = blocksPlaced;
			blocksToPlace += printer.markAllBlockRequirements(checklist, level, this::shouldPlace);
			printer.markAllEntityRequirements(checklist);
		}

		checklist.gathered.clear();
		findInventories();
		for (IItemHandler inventory : attachedInventories) {
			for (int slot = 0; slot < inventory.getSlots(); slot++) {
				ItemStack stackInSlot = inventory.getStackInSlot(slot);
				if (inventory.extractItem(slot, 1, true)
					.isEmpty())
					continue;
				checklist.collect(stackInSlot);
			}
		}
		sendUpdate = true;
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {}

	@Override
	public void lazyTick() {
		super.lazyTick();
		findInventories();
	}

	@Override
	public void preRemoveSideEffects(BlockPos pos, BlockState state) {
		if (level != null && !level.isClientSide())
			ItemHelper.dropContents(level, pos, inventory);
		super.preRemoveSideEffects(pos, state);
	}

	public enum State {
		STOPPED, PAUSED, RUNNING
	}

}

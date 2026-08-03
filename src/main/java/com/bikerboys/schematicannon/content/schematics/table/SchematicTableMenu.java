package com.bikerboys.schematicannon.content.schematics.table;

import com.bikerboys.schematicannon.AllItems;
import com.bikerboys.schematicannon.AllMenuTypes;
import com.bikerboys.schematicannon.Schematicannon;
import com.bikerboys.schematicannon.foundation.gui.menu.MenuBase;
import com.bikerboys.schematicannon.foundation.item.SlotItemHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;

public class SchematicTableMenu extends MenuBase<SchematicTableBlockEntity> {

	private Slot inputSlot;
	private Slot outputSlot;

	public SchematicTableMenu(MenuType<?> type, int id, Inventory inv, FriendlyByteBuf extraData) {
		super(type, id, inv, extraData);
	}

	public SchematicTableMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
		this(AllMenuTypes.SCHEMATIC_TABLE.get(), id, inv, extraData);
	}

	public SchematicTableMenu(int id, Inventory inv) {
		this(AllMenuTypes.SCHEMATIC_TABLE.get(), id, inv, findClientBlockEntity());
	}

	private static SchematicTableBlockEntity findClientBlockEntity() {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.level != null && minecraft.hitResult instanceof BlockHitResult hit
			&& minecraft.level.getBlockEntity(hit.getBlockPos()) instanceof SchematicTableBlockEntity table)
			return table;
		throw new IllegalStateException("Schematic table menu opened without a targeted table");
	}

	public SchematicTableMenu(MenuType<?> type, int id, Inventory inv, SchematicTableBlockEntity be) {
		super(type, id, inv, be);
	}

	public static SchematicTableMenu create(int id, Inventory inv, SchematicTableBlockEntity be) {
		return new SchematicTableMenu(AllMenuTypes.SCHEMATIC_TABLE.get(), id, inv, be);
	}

	public boolean canWrite() {
		return inputSlot.hasItem() && !outputSlot.hasItem();
	}

	public boolean placeCarriedEmptySchematic() {
		ItemStack carried = getCarried();
		if (inputSlot.hasItem() || !carried.is(AllItems.EMPTY_SCHEMATIC.get()))
			return false;
		inputSlot.setByPlayer(carried.copyWithCount(1));
		ItemStack remainder = carried.copy();
		remainder.shrink(1);
		setCarried(remainder);
		broadcastChanges();
		return true;
	}

	public boolean hasEmptySchematicAvailable(Player player) {
		if (getCarried().is(AllItems.EMPTY_SCHEMATIC.get()))
			return true;
		Inventory inventory = player.getInventory();
		for (int slot = 0; slot < inventory.getContainerSize(); slot++)
			if (inventory.getItem(slot).is(AllItems.EMPTY_SCHEMATIC.get()))
				return true;
		return false;
	}

	public boolean placeEmptySchematicFromPlayer(Player player) {
		if (inputSlot.hasItem())
			return false;

		ItemStack source = getCarried();
		if (source.is(AllItems.EMPTY_SCHEMATIC.get())) {
			ItemStack remainder = source.copy();
			remainder.shrink(1);
			setCarried(remainder);
		} else {
			Inventory inventory = player.getInventory();
			int sourceSlot = -1;
			for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
				if (!inventory.getItem(slot).is(AllItems.EMPTY_SCHEMATIC.get()))
					continue;
				sourceSlot = slot;
				break;
			}
			if (sourceSlot == -1)
				return false;
			inventory.removeItem(sourceSlot, 1);
			inventory.setChanged();
		}

		inputSlot.setByPlayer(new ItemStack(AllItems.EMPTY_SCHEMATIC.get()));
		broadcastChanges();
		return true;
	}

	@Override
	public void clicked(int slotId, int button, ContainerInput input, Player player) {
		if (slotId == inputSlot.index || getCarried().is(AllItems.EMPTY_SCHEMATIC.get()))
			Schematicannon.LOGGER.info(
				"TABLE_SLOT_DIAG menuClick side={} slotId={} inputSlotIndex={} button={} input={} carried={} inputStack={}",
				player.level().isClientSide() ? "client" : "server", slotId, inputSlot.index, button, input,
				getCarried(), inputSlot.getItem());
		if (slotId == inputSlot.index && input == ContainerInput.PICKUP && !inputSlot.hasItem()
			&& placeCarriedEmptySchematic())
			return;
		super.clicked(slotId, button, input, player);
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		Slot clickedSlot = getSlot(index);
		if (!clickedSlot.hasItem())
			return ItemStack.EMPTY;

		ItemStack stack = clickedSlot.getItem();
		if (stack.is(AllItems.EMPTY_SCHEMATIC.get()))
			Schematicannon.LOGGER.info(
				"TABLE_SLOT_DIAG quickMove side={} index={} inputSlotIndex={} stack={} inputStack={}",
				player.level().isClientSide() ? "client" : "server", index, inputSlot.index, stack, inputSlot.getItem());
		ItemStack original = stack.copy();
		if (index < 2) {
			if (!moveItemStackTo(stack, 2, slots.size(), false))
				return ItemStack.EMPTY;
		} else if (!moveItemStackTo(stack, 0, 1, false)) {
			return ItemStack.EMPTY;
		}

		if (stack.isEmpty())
			clickedSlot.set(ItemStack.EMPTY);
		else
			clickedSlot.setChanged();

		if (stack.getCount() == original.getCount())
			return ItemStack.EMPTY;

		clickedSlot.onTake(player, stack);
		return original;
	}

	@Override
	protected SchematicTableBlockEntity createOnClient(FriendlyByteBuf extraData) {
		ClientLevel world = Minecraft.getInstance().level;
		BlockEntity blockEntity = world.getBlockEntity(extraData.readBlockPos());
		if (blockEntity instanceof SchematicTableBlockEntity schematicTable) {
			schematicTable.readClient(extraData.readNbt());
			return schematicTable;
		}
		return null;
	}

	@Override
	protected void initAndReadInventory(SchematicTableBlockEntity contentHolder) {
	}

	@Override
	protected void addSlots() {
		inputSlot = new SlotItemHandler(contentHolder.inventory, 0, 21, 59) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return contentHolder.inventory.isItemValid(0, stack);
			}
		};

		outputSlot = new SlotItemHandler(contentHolder.inventory, 1, 166, 59) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return false;
			}
		};

		addSlot(inputSlot);
		addSlot(outputSlot);

		// player Slots
		for (int row = 0; row < 3; ++row) {
			for (int col = 0; col < 9; ++col) {
				this.addSlot(new Slot(player.getInventory(), col + row * 9 + 9, 38 + col * 18, 107 + row * 18));
			}
		}

		for (int hotbarSlot = 0; hotbarSlot < 9; ++hotbarSlot) {
			this.addSlot(new Slot(player.getInventory(), hotbarSlot, 38 + hotbarSlot * 18, 165));
		}
	}

	@Override
	protected void saveData(SchematicTableBlockEntity contentHolder) {
	}

}

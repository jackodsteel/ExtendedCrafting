package com.blakebr0.extendedcrafting.container;

import com.blakebr0.cucumber.inventory.BaseItemStackHandler;
import com.blakebr0.cucumber.inventory.slot.OutputSlot;
import com.blakebr0.extendedcrafting.api.crafting.ITableRecipe;
import com.blakebr0.extendedcrafting.api.crafting.RecipeTypes;
import com.blakebr0.extendedcrafting.container.inventory.ExtendedCraftingInventory;
import com.blakebr0.extendedcrafting.container.slot.TableOutputSlot;
import com.blakebr0.extendedcrafting.init.ModContainerTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftResultInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IIntArray;
import net.minecraft.util.IntArray;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Optional;
import java.util.function.Function;

public class AdvancedAutoTableContainer extends Container {
	private final Function<PlayerEntity, Boolean> isUsableByPlayer;
	private final IIntArray data;
	private final BlockPos pos;
	private final World world;
	private final IInventory result;

	private AdvancedAutoTableContainer(ContainerType<?> type, int id, PlayerInventory playerInventory, PacketBuffer buffer) {
		this(type, id, playerInventory, p -> false, new BaseItemStackHandler(26), new IntArray(6), buffer.readBlockPos());
	}

	private AdvancedAutoTableContainer(ContainerType<?> type, int id, PlayerInventory playerInventory, Function<PlayerEntity, Boolean> isUsableByPlayer, BaseItemStackHandler inventory, IIntArray data, BlockPos pos) {
		super(type, id);
		this.isUsableByPlayer = isUsableByPlayer;
		this.data = data;
		this.pos = pos;
		this.world = playerInventory.player.level;
		this.result = new CraftResultInventory();

		IInventory matrix = new ExtendedCraftingInventory(this, inventory, 5, true);

		this.addSlot(new TableOutputSlot(this, matrix, this.result, 0, 154, 37));
		
		int i, j;
		for (i = 0; i < 5; i++) {
			for (j = 0; j < 5; j++) {
				this.addSlot(new Slot(matrix, j + i * 5, 26 + j * 18, 18 + i * 18));
			}
		}

		this.addSlot(new OutputSlot(inventory, 25, 154, 81));

		for (i = 0; i < 3; i++) {
			for (j = 0; j < 9; j++) {
				this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 13 + j * 18, 124 + i * 18));
			}
		}

		for (j = 0; j < 9; j++) {
			this.addSlot(new Slot(playerInventory, j, 13 + j * 18, 182));
		}

		this.slotsChanged(matrix);
		this.addDataSlots(data);
	}

	@Override
	public void slotsChanged(IInventory matrix) {
		Optional<ITableRecipe> recipe = this.world.getRecipeManager().getRecipeFor(RecipeTypes.TABLE, matrix, this.world);
		if (recipe.isPresent()) {
			ItemStack result = recipe.get().assemble(matrix);
			this.result.setItem(0, result);
		} else {
			this.result.setItem(0, ItemStack.EMPTY);
		}

		super.slotsChanged(matrix);
	}

	@Override
	public boolean stillValid(PlayerEntity player) {
		return this.isUsableByPlayer.apply(player);
	}

	@Override
	public ItemStack quickMoveStack(PlayerEntity player, int slotNumber) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.slots.get(slotNumber);

		if (slot != null && slot.hasItem()) {
			ItemStack itemstack1 = slot.getItem();
			itemstack = itemstack1.copy();

			if (slotNumber == 0 || slotNumber == 26) {
				if (!this.moveItemStackTo(itemstack1, 27, 63, true)) {
					return ItemStack.EMPTY;
				}

				slot.onQuickCraft(itemstack1, itemstack);
			} else if (slotNumber >= 27 && slotNumber < 63) {
				if (!this.moveItemStackTo(itemstack1, 1, 26, false)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.moveItemStackTo(itemstack1, 27, 63, false)) {
				return ItemStack.EMPTY;
			}

			if (itemstack1.isEmpty()) {
				slot.set(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}

			if (itemstack1.getCount() == itemstack.getCount()) {
				return ItemStack.EMPTY;
			}

			slot.onTake(player, itemstack1);
		}

		return itemstack;
	}

	public BlockPos getPos() {
		return this.pos;
	}

	public static AdvancedAutoTableContainer create(int windowId, PlayerInventory playerInventory, PacketBuffer buffer) {
		return new AdvancedAutoTableContainer(ModContainerTypes.ADVANCED_AUTO_TABLE.get(), windowId, playerInventory, buffer);
	}

	public static AdvancedAutoTableContainer create(int windowId, PlayerInventory playerInventory, Function<PlayerEntity, Boolean> isUsableByPlayer, BaseItemStackHandler inventory, IIntArray data, BlockPos pos) {
		return new AdvancedAutoTableContainer(ModContainerTypes.ADVANCED_AUTO_TABLE.get(), windowId, playerInventory, isUsableByPlayer, inventory, data, pos);
	}
}
package com.blakebr0.extendedcrafting.container;

import com.blakebr0.cucumber.inventory.BaseItemStackHandler;
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
import net.minecraft.world.World;

import java.util.Optional;
import java.util.function.Function;

public class AdvancedTableContainer extends Container {
	private final Function<PlayerEntity, Boolean> isUsableByPlayer;
	private final World world;
	private final IInventory result;

	private AdvancedTableContainer(ContainerType<?> type, int id, PlayerInventory playerInventory) {
		this(type, id, playerInventory, p -> false, new BaseItemStackHandler(25));
	}

	private AdvancedTableContainer(ContainerType<?> type, int id, PlayerInventory playerInventory, Function<PlayerEntity, Boolean> isUsableByPlayer, BaseItemStackHandler inventory) {
		super(type, id);
		this.isUsableByPlayer = isUsableByPlayer;
		this.world = playerInventory.player.level;
		this.result = new CraftResultInventory();

		IInventory matrix = new ExtendedCraftingInventory(this, inventory, 5);

		this.addSlot(new TableOutputSlot(this, matrix, this.result, 0, 142, 53));
		
		int i, j;
		for (i = 0; i < 5; i++) {
			for (j = 0; j < 5; j++) {
				this.addSlot(new Slot(matrix, j + i * 5, 14 + j * 18, 18 + i * 18));
			}
		}

		for (i = 0; i < 3; i++) {
			for (j = 0; j < 9; j++) {
				this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 124 + i * 18));
			}
		}

		for (j = 0; j < 9; j++) {
			this.addSlot(new Slot(playerInventory, j, 8 + j * 18, 182));
		}

		this.slotsChanged(matrix);
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

			if (slotNumber == 0) {
				if (!this.moveItemStackTo(itemstack1, 26, 62, true)) {
					return ItemStack.EMPTY;
				}

				slot.onQuickCraft(itemstack1, itemstack);
			} else if (slotNumber >= 26 && slotNumber < 62) {
				if (!this.moveItemStackTo(itemstack1, 1, 26, false)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.moveItemStackTo(itemstack1, 26, 62, false)) {
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

	public static AdvancedTableContainer create(int windowId, PlayerInventory playerInventory) {
		return new AdvancedTableContainer(ModContainerTypes.ADVANCED_TABLE.get(), windowId, playerInventory);
	}

	public static AdvancedTableContainer create(int windowId, PlayerInventory playerInventory, Function<PlayerEntity, Boolean> isUsableByPlayer, BaseItemStackHandler inventory) {
		return new AdvancedTableContainer(ModContainerTypes.ADVANCED_TABLE.get(), windowId, playerInventory, isUsableByPlayer, inventory);
	}
}
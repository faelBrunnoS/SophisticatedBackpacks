package net.p3pp3rf1y.sophisticatedbackpacks.upgrades.cooking;

import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.AbstractCookingRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.items.ItemStackHandler;
import net.p3pp3rf1y.sophisticatedbackpacks.Config;
import net.p3pp3rf1y.sophisticatedbackpacks.util.NBTHelper;
import net.p3pp3rf1y.sophisticatedbackpacks.util.RecipeHelper;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class CookingLogic<T extends AbstractCookingRecipe> {
	private final ItemStack upgrade;
	private final Consumer<ItemStack> saveHandler;

	private ItemStackHandler cookingInventory = null;
	public static final int COOK_INPUT_SLOT = 0;
	public static final int COOK_OUTPUT_SLOT = 2;
	public static final int FUEL_SLOT = 1;
	@Nullable
	private T cookingRecipe = null;
	private boolean cookingRecipeInitialized = false;

	private final float burnTimeModifier;
	private final Predicate<ItemStack> isFuel;
	private final Predicate<ItemStack> isInput;
	private final double cookingSpeedMultiplier;
	private final double fuelEfficiencyMultiplier;
	private final IRecipeType<T> recipeType;

	public CookingLogic(ItemStack upgrade, Consumer<ItemStack> saveHandler, Config.Common.CookingUpgradeConfig cookingUpgradeConfig, IRecipeType<T> recipeType, float burnTimeModifier) {
		this(upgrade, saveHandler, s -> getBurnTime(s, recipeType, burnTimeModifier) > 0, s -> RecipeHelper.getCookingRecipe(s, recipeType).isPresent(), cookingUpgradeConfig, recipeType, burnTimeModifier);
	}

	public CookingLogic(ItemStack upgrade, Consumer<ItemStack> saveHandler, Predicate<ItemStack> isFuel, Predicate<ItemStack> isInput, Config.Common.CookingUpgradeConfig cookingUpgradeConfig, IRecipeType<T> recipeType, float burnTimeModifier) {
		this.upgrade = upgrade;
		this.saveHandler = saveHandler;
		this.isFuel = isFuel;
		this.isInput = isInput;
		cookingSpeedMultiplier = cookingUpgradeConfig.cookingSpeedMultiplier.get();
		fuelEfficiencyMultiplier = cookingUpgradeConfig.fuelEfficiencyMultiplier.get();
		this.recipeType = recipeType;
		this.burnTimeModifier = burnTimeModifier;
	}

	private void save() {
		saveHandler.accept(upgrade);
	}

	public boolean tick(World world) {
		AtomicBoolean didSomething = new AtomicBoolean(true);
		if (isBurning(world) || readyToStartCooking()) {
			Optional<T> fr = getCookingRecipe();
			if (!fr.isPresent() && isCooking()) {
				setIsCooking(false);
			}
			fr.ifPresent(recipe -> {
				updateFuel(world, recipe);

				if (isBurning(world) && canSmelt(recipe)) {
					updateCookingProgress(world, recipe);
				} else if (!isBurning(world)) {
					didSomething.set(false);
				}
			});
		}

		if (!isBurning(world) && isCooking()) {
			updateCookingCooldown(world);
		} else {
			didSomething.set(false);
		}
		return didSomething.get();
	}

	public boolean isBurning(World world) {
		return getBurnTimeFinish() >= world.getGameTime();
	}

	private Optional<T> getCookingRecipe() {
		if (!cookingRecipeInitialized) {
			cookingRecipe = RecipeHelper.getCookingRecipe(getCookInput(), recipeType).orElse(null);
			cookingRecipeInitialized = true;
		}
		return Optional.ofNullable(cookingRecipe);
	}

	private void updateCookingCooldown(World world) {
		if (getRemainingCookTime(world) + 2 > getCookTimeTotal()) {
			setIsCooking(false);
		} else {
			setCookTimeFinish(world.getGameTime() + Math.min(getRemainingCookTime(world) + 2, getCookTimeTotal()));
		}
	}

	private void updateCookingProgress(World world, T cookingRecipe) {
		if (isCooking() && finishedCooking(world)) {
			smelt(cookingRecipe);
			if (canSmelt(cookingRecipe)) {
				setCookTime(world, (int) (cookingRecipe.getCookingTime() * (1 / cookingSpeedMultiplier)));
			} else {
				setIsCooking(false);
			}
		} else if (!isCooking()) {
			setIsCooking(true);
			setCookTime(world, (int) (cookingRecipe.getCookingTime() * (1 / cookingSpeedMultiplier)));
		}
	}

	private boolean finishedCooking(World world) {
		return getCookTimeFinish() <= world.getGameTime();
	}

	private boolean readyToStartCooking() {
		return !getFuel().isEmpty() && !getCookInput().isEmpty();
	}

	private void smelt(IRecipe<?> recipe) {
		if (!canSmelt(recipe)) {
			return;
		}

		ItemStack input = getCookInput();
		ItemStack recipeOutput = recipe.getResultItem();
		ItemStack output = getCookOutput();
		if (output.isEmpty()) {
			setCookOutput(recipeOutput.copy());
		} else if (output.getItem() == recipeOutput.getItem()) {
			output.grow(recipeOutput.getCount());
			setCookOutput(output);
		}

		if (input.getItem() == Blocks.WET_SPONGE.asItem() && !getFuel().isEmpty() && getFuel().getItem() == Items.BUCKET) {
			setFuel(new ItemStack(Items.WATER_BUCKET));
		}

		input.shrink(1);
		setCookInput(input);
	}

	public void setCookInput(ItemStack input) {
		cookingInventory.setStackInSlot(COOK_INPUT_SLOT, input);
	}

	private void setCookOutput(ItemStack stack) {
		getCookingInventory().setStackInSlot(COOK_OUTPUT_SLOT, stack);
	}

	private int getRemainingCookTime(World world) {
		return (int) (getCookTimeFinish() - world.getGameTime());
	}

	private void setCookTime(World world, int cookTime) {
		setCookTimeFinish(world.getGameTime() + cookTime);
		setCookTimeTotal(cookTime);
	}

	private void updateFuel(World world, T cookingRecipe) {
		ItemStack fuel = getFuel();
		if (!isBurning(world) && canSmelt(cookingRecipe)) {
			if (getBurnTime(fuel, recipeType, burnTimeModifier) <= 0) {
				return;
			}
			setBurnTime(world, (int) (getBurnTime(fuel, recipeType, burnTimeModifier) * fuelEfficiencyMultiplier / cookingSpeedMultiplier));
			if (isBurning(world)) {
				if (fuel.hasContainerItem()) {
					setFuel(fuel.getContainerItem());
				} else if (!fuel.isEmpty()) {
					fuel.shrink(1);
					setFuel(fuel);
					if (fuel.isEmpty()) {
						setFuel(fuel.getContainerItem());
					}
				}
			}
		}
	}

	private void setBurnTime(World world, int burnTime) {
		setBurnTimeFinish(world.getGameTime() + burnTime);
		setBurnTimeTotal(burnTime);
	}

	protected boolean canSmelt(IRecipe<?> cookingRecipe) {
		if (getCookInput().isEmpty()) {
			return false;
		}
		ItemStack recipeOutput = cookingRecipe.getResultItem();
		if (recipeOutput.isEmpty()) {
			return false;
		} else {
			ItemStack output = getCookOutput();
			if (output.isEmpty()) {
				return true;
			} else if (!output.sameItem(recipeOutput)) {
				return false;
			} else if (output.getCount() + recipeOutput.getCount() <= 64 && output.getCount() + recipeOutput.getCount() <= output.getMaxStackSize()) {
				return true;
			} else {
				return output.getCount() + recipeOutput.getCount() <= recipeOutput.getMaxStackSize();
			}
		}
	}

	private static <T extends AbstractCookingRecipe> int getBurnTime(ItemStack fuel, IRecipeType<T> recipeType, float burnTimeModifier) {
		return (int) (ForgeHooks.getBurnTime(fuel, recipeType) * burnTimeModifier);
	}

	public ItemStack getCookOutput() {
		return getCookingInventory().getStackInSlot(COOK_OUTPUT_SLOT);
	}

	public ItemStack getCookInput() {
		return getCookingInventory().getStackInSlot(COOK_INPUT_SLOT);
	}

	public ItemStack getFuel() {
		return getCookingInventory().getStackInSlot(FUEL_SLOT);
	}

	public void setFuel(ItemStack fuel) {
		getCookingInventory().setStackInSlot(FUEL_SLOT, fuel);
	}

	public ItemStackHandler getCookingInventory() {
		if (cookingInventory == null) {
			cookingInventory = new ItemStackHandler(3) {
				@Override
				protected void onContentsChanged(int slot) {
					super.onContentsChanged(slot);
					upgrade.addTagElement("cookingInventory", serializeNBT());
					save();
					if (slot == COOK_INPUT_SLOT) {
						cookingRecipeInitialized = false;
					}
				}

				@Override
				public boolean isItemValid(int slot, ItemStack stack) {
					switch (slot) {
						case COOK_INPUT_SLOT:
							return isInput.test(stack);
						case FUEL_SLOT:
							return isFuel.test(stack);
						default:
							return true;
					}
				}
			};

			//TODO in the future remove use of this legacy smeltingInventory load as it should no longer be required
			Optional<CompoundNBT> smeltingInventory = NBTHelper.getCompound(upgrade, "smeltingInventory");
			if (smeltingInventory.isPresent()) {
				cookingInventory.deserializeNBT(smeltingInventory.get());
			} else {
				NBTHelper.getCompound(upgrade, "cookingInventory").ifPresent(cookingInventory::deserializeNBT);
			}
		}
		return cookingInventory;
	}

	public long getBurnTimeFinish() {
		return NBTHelper.getLong(upgrade, "burnTimeFinish").orElse(0L);
	}

	private void setBurnTimeFinish(long burnTimeFinish) {
		NBTHelper.setLong(upgrade, "burnTimeFinish", burnTimeFinish);
		save();
	}

	public int getBurnTimeTotal() {
		return NBTHelper.getInt(upgrade, "burnTimeTotal").orElse(0);
	}

	private void setBurnTimeTotal(int burnTimeTotal) {
		NBTHelper.setInteger(upgrade, "burnTimeTotal", burnTimeTotal);
		save();
	}

	public long getCookTimeFinish() {
		return NBTHelper.getLong(upgrade, "cookTimeFinish").orElse(-1L);
	}

	private void setCookTimeFinish(long cookTimeFinish) {
		NBTHelper.setLong(upgrade, "cookTimeFinish", cookTimeFinish);
		save();
	}

	public int getCookTimeTotal() {
		return NBTHelper.getInt(upgrade, "cookTimeTotal").orElse(0);
	}

	private void setCookTimeTotal(int cookTimeTotal) {
		NBTHelper.setInteger(upgrade, "cookTimeTotal", cookTimeTotal);
		save();
	}

	public boolean isCooking() {
		return NBTHelper.getBoolean(upgrade, "isCooking").orElse(false);
	}

	private void setIsCooking(boolean isCooking) {
		NBTHelper.setBoolean(upgrade, "isCooking", isCooking);
		save();
	}
}

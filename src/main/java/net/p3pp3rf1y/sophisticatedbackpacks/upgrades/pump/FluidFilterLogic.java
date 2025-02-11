package net.p3pp3rf1y.sophisticatedbackpacks.upgrades.pump;

import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.registries.ForgeRegistries;
import net.p3pp3rf1y.sophisticatedbackpacks.util.NBTHelper;

import java.util.List;
import java.util.function.Consumer;

public class FluidFilterLogic {
	private final List<Fluid> fluidFilters;
	private final ItemStack upgrade;
	private final Consumer<ItemStack> saveHandler;
	private boolean noFilter = true;

	public FluidFilterLogic(int filterSlots, ItemStack upgrade, Consumer<ItemStack> saveHandler) {
		fluidFilters = NonNullList.withSize(filterSlots, Fluids.EMPTY);
		this.upgrade = upgrade;
		this.saveHandler = saveHandler;
		deserializeFluidFilters();
		updateNoFilter();
	}

	private void deserializeFluidFilters() {
		NBTHelper.getTagValue(upgrade, "", "fluids", (c, n1) -> c.getList(n1, Constants.NBT.TAG_STRING)).ifPresent(listNbt -> {
			int i = 0;
			for (INBT elementNbt : listNbt) {
				Fluid value = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(elementNbt.getAsString()));
				if (value != null) {
					fluidFilters.set(i, value);
				}
				i++;
				if (i >= fluidFilters.size()) {
					break;
				}
			}
		});
	}

	private void updateNoFilter() {
		noFilter = true;
		for (Fluid fluidFilter : fluidFilters) {
			if (fluidFilter != Fluids.EMPTY) {
				noFilter = false;
				return;
			}
		}
	}

	public boolean fluidMatches(Fluid fluid) {
		return noFilter || matchesFluidFilter(fluid);
	}

	private boolean matchesFluidFilter(Fluid fluid) {
		for (Fluid fluidFilter : fluidFilters) {
			if (fluidFilter == fluid) {
				return true;
			}
		}
		return false;
	}

	private void save() {
		saveHandler.accept(upgrade);
	}

	public void setFluid(int index, Fluid fluid) {
		fluidFilters.set(index, fluid);
		serializeFluidFilters();
		updateNoFilter();
		save();
	}

	public Fluid getFluid(int index) {
		return fluidFilters.get(index);
	}

	public int getNumberOfFluidFilters() {
		return fluidFilters.size();
	}

	private void serializeFluidFilters() {
		ListNBT fluids = new ListNBT();
		//noinspection ConstantConditions - only registered fluids get added
		fluidFilters.forEach(f -> fluids.add(StringNBT.valueOf(f.getRegistryName().toString())));
		upgrade.getOrCreateTag().put("fluids", fluids);
	}
}

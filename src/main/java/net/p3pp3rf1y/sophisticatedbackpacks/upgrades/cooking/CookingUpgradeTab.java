package net.p3pp3rf1y.sophisticatedbackpacks.upgrades.cooking;

import net.minecraft.item.crafting.AbstractCookingRecipe;
import net.minecraft.item.crafting.BlastingRecipe;
import net.minecraft.item.crafting.FurnaceRecipe;
import net.minecraft.item.crafting.SmokingRecipe;
import net.p3pp3rf1y.sophisticatedbackpacks.client.gui.BackpackScreen;
import net.p3pp3rf1y.sophisticatedbackpacks.client.gui.UpgradeSettingsTab;
import net.p3pp3rf1y.sophisticatedbackpacks.client.gui.utils.Position;

import static net.p3pp3rf1y.sophisticatedbackpacks.client.gui.utils.TranslationHelper.translUpgrade;
import static net.p3pp3rf1y.sophisticatedbackpacks.client.gui.utils.TranslationHelper.translUpgradeTooltip;

public abstract class CookingUpgradeTab<R extends AbstractCookingRecipe, W extends CookingUpgradeWrapper<W, ?, R>>
		extends UpgradeSettingsTab<CookingUpgradeContainer<R, W>> {
	private final CookingLogicControl<R> cookingLogicControl;

	protected CookingUpgradeTab(CookingUpgradeContainer<R, W> upgradeContainer, Position position, BackpackScreen screen, String tabLabel, String closedTooltip) {
		super(upgradeContainer, position, screen, translUpgrade(tabLabel), translUpgradeTooltip(closedTooltip));
		cookingLogicControl = addHideableChild(new CookingLogicControl<R>(new Position(x + 3, y + 24), getContainer().getSmeltingLogicContainer()));
	}

	@Override
	protected void moveSlotsToTab() {
		cookingLogicControl.moveSlotsToView(screen.getGuiLeft(), screen.getGuiTop());
	}

	public static class SmeltingUpgradeTab extends CookingUpgradeTab<FurnaceRecipe, CookingUpgradeWrapper.SmeltingUpgradeWrapper> {
		public SmeltingUpgradeTab(CookingUpgradeContainer<FurnaceRecipe, CookingUpgradeWrapper.SmeltingUpgradeWrapper> upgradeContainer, Position position, BackpackScreen screen) {
			super(upgradeContainer, position, screen, "smelting", "smelting");
		}
	}

	public static class SmokingUpgradeTab extends CookingUpgradeTab<SmokingRecipe, CookingUpgradeWrapper.SmokingUpgradeWrapper> {
		public SmokingUpgradeTab(CookingUpgradeContainer<SmokingRecipe, CookingUpgradeWrapper.SmokingUpgradeWrapper> upgradeContainer, Position position, BackpackScreen screen) {
			super(upgradeContainer, position, screen, "smoking", "smoking");
		}
	}

	public static class BlastingUpgradeTab extends CookingUpgradeTab<BlastingRecipe, CookingUpgradeWrapper.BlastingUpgradeWrapper> {
		public BlastingUpgradeTab(CookingUpgradeContainer<BlastingRecipe, CookingUpgradeWrapper.BlastingUpgradeWrapper> upgradeContainer, Position position, BackpackScreen screen) {
			super(upgradeContainer, position, screen, "blasting", "blasting");
		}
	}
}

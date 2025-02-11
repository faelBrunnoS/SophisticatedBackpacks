package net.p3pp3rf1y.sophisticatedbackpacks.upgrades.pump;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.p3pp3rf1y.sophisticatedbackpacks.common.gui.UpgradeContainerBase;
import net.p3pp3rf1y.sophisticatedbackpacks.common.gui.UpgradeContainerType;

public class PumpUpgradeContainer extends UpgradeContainerBase<PumpUpgradeWrapper, PumpUpgradeContainer> {
	private static final String DATA_IS_INPUT = "isInput";
	private static final String DATA_INTERACT_WITH_HAND = "interactWithHand";
	private static final String DATA_INTERACT_WITH_WORLD = "interactWithWorld";
	private final FluidFilterContainer fluidFilterContainer;

	public PumpUpgradeContainer(PlayerEntity player, int upgradeContainerId, PumpUpgradeWrapper upgradeWrapper, UpgradeContainerType<PumpUpgradeWrapper, PumpUpgradeContainer> type) {
		super(player, upgradeContainerId, upgradeWrapper, type);

		fluidFilterContainer = new FluidFilterContainer(player, this, upgradeWrapper::getFluidFilterLogic);
	}

	public void setIsInput(boolean input) {
		upgradeWrapper.setIsInput(input);
		sendBooleanToServer(DATA_IS_INPUT, input);
	}

	public boolean isInput() {
		return upgradeWrapper.isInput();
	}

	@Override
	public void handleMessage(CompoundNBT data) {
		if (data.contains(DATA_IS_INPUT)) {
			setIsInput(data.getBoolean(DATA_IS_INPUT));
		} else if (data.contains(DATA_INTERACT_WITH_HAND)) {
			setInteractWithHand(data.getBoolean(DATA_INTERACT_WITH_HAND));
		} else if (data.contains(DATA_INTERACT_WITH_WORLD)) {
			setInteractWithWorld(data.getBoolean(DATA_INTERACT_WITH_WORLD));
		}
		fluidFilterContainer.handleMessage(data);
	}

	public FluidFilterContainer getFluidFilterContainer() {
		return fluidFilterContainer;
	}

	public void setInteractWithHand(boolean interactWithHand) {
		upgradeWrapper.setInteractWithHand(interactWithHand);
		sendBooleanToServer(DATA_INTERACT_WITH_HAND, interactWithHand);
	}

	public boolean shouldInteractWithHand() {
		return upgradeWrapper.shouldInteractWithHand();
	}

	public void setInteractWithWorld(boolean interactWithWorld) {
		upgradeWrapper.setInteractWithWorld(interactWithWorld);
		sendBooleanToServer(DATA_INTERACT_WITH_WORLD, interactWithWorld);
	}

	public boolean shouldInteractWithWorld() {
		return upgradeWrapper.shouldInteractWithWorld();
	}
}

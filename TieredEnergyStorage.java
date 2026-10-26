package com.techquest.tier;

import com.techquest.energy.MachineEnergyStorage;

/** FE-хранилище с тиром: машины принимают только <= своего тира. */
public class TieredEnergyStorage extends MachineEnergyStorage {
    private final EnergyTier tier;
    private boolean overloaded;

    public TieredEnergyStorage(int capacity, EnergyTier tier) {
        super(capacity, tier.transfer(), tier.transfer());
        this.tier = tier;
    }

    public EnergyTier getTier() { return tier; }
    public boolean isOverloaded() { return overloaded; }
    public void clearOverload() { overloaded = false; }

    /** Вызвать при входящем пакете с тиром источника. */
    public int receiveFromTier(int amount, EnergyTier sourceTier, boolean simulate) {
        if (!tier.canAccept(sourceTier)) {
            if (!simulate) overloaded = true;
            return 0;
        }
        return receiveEnergy(amount, simulate);
    }
}

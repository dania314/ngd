package com.techquest.energy;

import net.neoforged.neoforge.energy.EnergyStorage;

/**
 * Базовое хранилище энергии (FE) для всех машин мода.
 * Внешние сети используют receiveEnergy/extractEnergy (с лимитами),
 * сама машина — addInternal/removeInternal (без лимитов).
 */
public class MachineEnergyStorage extends EnergyStorage {

    public MachineEnergyStorage(int capacity, int maxReceive, int maxExtract) {
        super(capacity, maxReceive, maxExtract);
    }

    /** Добавить энергию изнутри машины. Возвращает, сколько реально добавлено. */
    public int addInternal(int amount) {
        int added = Math.min(this.capacity - this.energy, amount);
        this.energy += added;
        return added;
    }

    /** Потратить энергию изнутри машины. Возвращает, сколько реально потрачено. */
    public int removeInternal(int amount) {
        int removed = Math.min(this.energy, amount);
        this.energy -= removed;
        return removed;
    }

    public void setEnergy(int value) {
        this.energy = Math.max(0, Math.min(value, this.capacity));
    }
}

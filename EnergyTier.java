package com.techquest.tier;

/** Уровни напряжения. Подача выше тира — перегрузка машины. */
public enum EnergyTier {
    LV(32, 128, "lv"),
    MV(128, 512, "mv"),
    HV(512, 2048, "hv"),
    EV(2048, 8192, "ev");

    private final int voltage;
    private final int transferPerTick;
    private final String id;

    EnergyTier(int voltage, int transferPerTick, String id) {
        this.voltage = voltage;
        this.transferPerTick = transferPerTick;
        this.id = id;
    }

    public int voltage() { return voltage; }
    public int transfer() { return transferPerTick; }
    public String id() { return id; }

    public boolean canAccept(EnergyTier incoming) {
        return incoming.ordinal() <= this.ordinal();
    }

    public static EnergyTier byId(String id) {
        for (EnergyTier t : values()) if (t.id.equals(id)) return t;
        return LV;
    }
}

package com.techquest.conveyor;

public enum ConveyorType {
    /** Открытая лента: может гореть, пропускает радиацию */
    NORMAL(0.05f, true, true),
    /** Герметичная: не горит от обычных предметов, держит радиацию */
    SEALED(0.03f, false, false);

    private final float speed;
    private final boolean canIgnite;
    private final boolean leaksRadiation;

    ConveyorType(float speed, boolean canIgnite, boolean leaksRadiation) {
        this.speed = speed;
        this.canIgnite = canIgnite;
        this.leaksRadiation = leaksRadiation;
    }

    public float speed() { return speed; }
    public boolean canIgnite() { return canIgnite; }
    public boolean leaksRadiation() { return leaksRadiation; }
}

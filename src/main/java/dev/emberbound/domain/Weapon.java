package dev.emberbound.domain;

public enum Weapon {
    FISTS("Fists", 0, 0),
    PISTOL("Pistol", 2, 25),
    SWORD("Sword", 3, 35),
    RIFLE("Rifle", 7, 45);

    private final String label;
    private final int damage;
    private final int price;

    Weapon(String label, int damage, int price) {
        this.label = label;
        this.damage = damage;
        this.price = price;
    }

    public String label() {
        return label;
    }

    public int damage() {
        return damage;
    }

    public int price() {
        return price;
    }
}

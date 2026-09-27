package dev.emberbound.domain;

/** Immutable starting builds; the original game's character statistics are preserved. */
public enum HeroClass {
    SAMURAI("Samurai", 5, 21, 15),
    ARCHER("Archer", 7, 18, 20),
    KNIGHT("Knight", 8, 24, 5);

    private final String label;
    private final int damage;
    private final int health;
    private final int gold;

    HeroClass(String label, int damage, int health, int gold) {
        this.label = label;
        this.damage = damage;
        this.health = health;
        this.gold = gold;
    }

    public String label() {
        return label;
    }

    public int damage() {
        return damage;
    }

    public int health() {
        return health;
    }

    public int gold() {
        return gold;
    }
}

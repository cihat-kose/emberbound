package dev.emberbound.domain;

public enum EnemyType {
    ZOMBIE("Zombie", 3, 10, 4),
    VAMPIRE("Vampire", 4, 14, 7),
    BEAR("Bear", 7, 20, 12);

    private final String label;
    private final int damage;
    private final int health;
    private final int gold;

    EnemyType(String label, int damage, int health, int gold) {
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

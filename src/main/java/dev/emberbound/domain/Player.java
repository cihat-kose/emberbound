package dev.emberbound.domain;

import java.util.Objects;

/** Owns health, equipment and money invariants; has no terminal or file dependencies. */
public final class Player {
    public static final int BANDAGE_HEALING = 10;
    public static final int BANDAGE_PRICE = 6;
    public static final int MAX_BANDAGES = 9;
    public static final int MAX_GOLD = 999_999;

    public enum Purchase {
        BOUGHT,
        NOT_ENOUGH_GOLD,
        NOT_AN_UPGRADE,
        BAG_FULL
    }

    private final String name;
    private final HeroClass hero;
    private int health;
    private int gold;
    private Weapon weapon;
    private Armor armor;
    private int bandages;

    public Player(String name, HeroClass hero) {
        this(name, hero, hero.health(), hero.gold(), Weapon.FISTS, Armor.NONE, 2);
    }

    public Player(
            String name,
            HeroClass hero,
            int health,
            int gold,
            Weapon weapon,
            Armor armor,
            int bandages) {
        this.name = validateName(name);
        this.hero = Objects.requireNonNull(hero);
        this.weapon = Objects.requireNonNull(weapon);
        this.armor = Objects.requireNonNull(armor);
        if (health < 0
                || health > hero.health()
                || gold < 0
                || gold > MAX_GOLD
                || bandages < 0
                || bandages > MAX_BANDAGES) {
            throw new IllegalArgumentException("Player statistics are outside their valid ranges.");
        }
        this.health = health;
        this.gold = gold;
        this.bandages = bandages;
    }

    public static String validateName(String name) {
        Objects.requireNonNull(name, "Name is required.");
        String trimmed = name.strip();
        if (trimmed.isEmpty()
                || trimmed.codePointCount(0, trimmed.length()) > 24
                || name.codePoints().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException(
                    "Use a name with 1-24 characters and no control characters.");
        }
        return trimmed;
    }

    public int takeDamage(int incoming) {
        requireAlive();
        if (incoming < 0) throw new IllegalArgumentException("Damage cannot be negative.");
        int taken = Math.min(health, Math.max(0, incoming - armor.block()));
        health -= taken;
        return taken;
    }

    public int healFully() {
        requireAlive();
        int healed = maxHealth() - health;
        health = maxHealth();
        return healed;
    }

    public int useBandage() {
        requireAlive();
        if (bandages == 0 || health == maxHealth()) return 0;
        bandages--;
        int healed = Math.min(BANDAGE_HEALING, maxHealth() - health);
        health += healed;
        return healed;
    }

    public int earnGold(int amount) {
        requireAlive();
        if (amount < 0 || amount > MAX_GOLD) {
            throw new IllegalArgumentException("Invalid gold reward.");
        }
        int credited = Math.min(amount, MAX_GOLD - gold);
        gold += credited;
        return credited;
    }

    public Purchase buy(Weapon item) {
        requireAlive();
        Objects.requireNonNull(item);
        if (item.damage() <= weapon.damage()) return Purchase.NOT_AN_UPGRADE;
        if (gold < item.price()) return Purchase.NOT_ENOUGH_GOLD;
        gold -= item.price();
        weapon = item;
        return Purchase.BOUGHT;
    }

    public Purchase buy(Armor item) {
        requireAlive();
        Objects.requireNonNull(item);
        if (item.block() <= armor.block()) return Purchase.NOT_AN_UPGRADE;
        if (gold < item.price()) return Purchase.NOT_ENOUGH_GOLD;
        gold -= item.price();
        armor = item;
        return Purchase.BOUGHT;
    }

    public Purchase buyBandage() {
        requireAlive();
        if (bandages == MAX_BANDAGES) return Purchase.BAG_FULL;
        if (gold < BANDAGE_PRICE) return Purchase.NOT_ENOUGH_GOLD;
        gold -= BANDAGE_PRICE;
        bandages++;
        return Purchase.BOUGHT;
    }

    private void requireAlive() {
        if (!isAlive()) throw new IllegalStateException("This expedition has ended.");
    }

    public String name() {
        return name;
    }

    public HeroClass hero() {
        return hero;
    }

    public int health() {
        return health;
    }

    public int maxHealth() {
        return hero.health();
    }

    public int gold() {
        return gold;
    }

    public Weapon weapon() {
        return weapon;
    }

    public Armor armor() {
        return armor;
    }

    public int bandages() {
        return bandages;
    }

    public int damage() {
        return hero.damage() + weapon.damage();
    }

    public boolean isAlive() {
        return health > 0;
    }
}

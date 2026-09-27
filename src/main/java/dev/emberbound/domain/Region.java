package dev.emberbound.domain;

public enum Region {
    CAVE("Cave", "Food", EnemyType.ZOMBIE, "Stone walls hide the last of the island's provisions."),
    FOREST(
            "Forest",
            "Firewood",
            EnemyType.VAMPIRE,
            "Beyond the treeline, something moves without a sound."),
    RIVER("River", "Water", EnemyType.BEAR, "Fresh water lies beyond the bears' hunting ground.");

    private final String label;
    private final String resource;
    private final EnemyType enemy;
    private final String description;

    Region(String label, String resource, EnemyType enemy, String description) {
        this.label = label;
        this.resource = resource;
        this.enemy = enemy;
        this.description = description;
    }

    public String label() {
        return label;
    }

    public String resource() {
        return resource;
    }

    public EnemyType enemy() {
        return enemy;
    }

    public String description() {
        return description;
    }
}

package dev.emberbound.domain;

public enum Armor {
    NONE("None", 0, 0),
    LIGHT("Light armor", 1, 15),
    MEDIUM("Medium armor", 3, 25),
    HEAVY("Heavy armor", 5, 40);

    private final String label;
    private final int block;
    private final int price;

    Armor(String label, int block, int price) {
        this.label = label;
        this.block = block;
        this.price = price;
    }

    public String label() {
        return label;
    }

    public int block() {
        return block;
    }

    public int price() {
        return price;
    }
}

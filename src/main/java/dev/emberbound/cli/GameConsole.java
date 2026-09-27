package dev.emberbound.cli;

import dev.emberbound.domain.Armor;
import dev.emberbound.domain.Expedition;
import dev.emberbound.domain.HeroClass;
import dev.emberbound.domain.Player;
import dev.emberbound.domain.Region;
import dev.emberbound.domain.Weapon;
import dev.emberbound.engine.Encounter;
import dev.emberbound.persistence.SaveStore;
import java.io.IOException;

/** Terminal presentation and navigation. Combat and economic rules live in the domain/engine. */
public final class GameConsole {
    private final Console console;
    private final SaveStore saves;
    private final long seed;

    public GameConsole(Console console, SaveStore saves, long seed) {
        this.console = console;
        this.saves = saves;
        this.seed = seed;
    }

    public void run() {
        console.line("\n  E M B E R B O U N D");
        console.line("  A Java survival adventure");
        console.line("  Three supplies. One signal fire. A way home.");
        try {
            Expedition expedition = titleMenu();
            if (expedition != null) play(expedition);
        } catch (Console.EndOfInput ignored) {
            console.line("\n  Input closed. Progress since your last save was not saved.");
        }
        console.line("  Until the next expedition.");
    }

    private Expedition titleMenu() {
        while (true) {
            console.section("MAIN MENU");
            console.line("  1  New expedition    2  Continue saved expedition");
            console.line("  3  How to play       0  Quit");
            switch (console.choice("Choose", 0, 3)) {
                case 0 -> {
                    return null;
                }
                case 1 -> {
                    return createPlayer();
                }
                case 2 -> {
                    try {
                        Expedition expedition = saves.load();
                        console.line(
                                "  Welcome back, "
                                        + expedition.player().name()
                                        + ". Your expedition is restored.");
                        return expedition;
                    } catch (IOException exception) {
                        console.line("  Could not load the expedition: " + exception.getMessage());
                        console.line("  Your existing save has not been changed.");
                    }
                }
                case 3 -> guide();
                default -> throw new IllegalStateException("Unexpected menu selection.");
            }
        }
    }

    private Expedition createPlayer() {
        String name;
        while (true) {
            try {
                name = Player.validateName(console.read("Survivor name (1-24 characters)"));
                break;
            } catch (IllegalArgumentException exception) {
                console.line("  " + exception.getMessage());
            }
        }
        console.section("CHOOSE YOUR SURVIVOR");
        for (HeroClass hero : HeroClass.values()) {
            console.line(
                    "  %d  %-8s  ATK %d  HP %d  GOLD %d"
                            .formatted(
                                    hero.ordinal() + 1,
                                    hero.label(),
                                    hero.damage(),
                                    hero.health(),
                                    hero.gold()));
        }
        HeroClass hero =
                HeroClass.values()[console.choice("Character", 1, HeroClass.values().length) - 1];
        Expedition expedition = Expedition.start(name, hero, seed);
        console.line(
                "  "
                        + name
                        + ", the last boat has gone. Gather supplies and light the signal fire.");
        console.line("  You start with 2 bandages. Rest at the Safe House whenever you need to.");
        console.line("  Expedition seed: " + seed);
        return expedition;
    }

    private void play(Expedition expedition) {
        while (expedition.player().isAlive() && !expedition.escaped()) {
            status(expedition);
            console.section("ISLAND MAP");
            console.line("  1  Safe House  - restore all health / return with supplies");
            console.line("  2  Tool Store  - weapons, armor and bandages");
            for (Region region : Region.values()) {
                var progress = expedition.progress(region);
                console.line(
                        "  %d  %-11s - %s | %s"
                                .formatted(
                                        region.ordinal() + 3,
                                        region.label(),
                                        region.resource(),
                                        progress.cleared()
                                                ? "CLEARED"
                                                : progress.remaining() + " enemies left"));
            }
            console.line("  6  Journal     7  Save expedition     0  Save and quit");
            switch (console.choice("Destination", 0, 7)) {
                case 0 -> {
                    if (save(expedition)) return;
                }
                case 1 -> {
                    int healed = expedition.rest();
                    console.line("  The hearth is warm. Restored " + healed + " HP.");
                    if (!expedition.escaped())
                        console.line("  Return here with Food, Firewood and Water to escape.");
                }
                case 2 -> shop(expedition.player());
                case 3 -> explore(expedition, Region.CAVE);
                case 4 -> explore(expedition, Region.FOREST);
                case 5 -> explore(expedition, Region.RIVER);
                case 6 -> journal(expedition);
                case 7 -> save(expedition);
                default -> throw new IllegalStateException("Unexpected destination.");
            }
        }
        if (expedition.escaped()) {
            console.section("YOU ESCAPED");
            console.line("  Food for the crossing. Water for the journey. Firewood for a signal.");
            console.line(
                    "  A sail appears beyond the fog. "
                            + expedition.player().name()
                            + ", you are going home.");
            console.line(
                    "  Enemies defeated: "
                            + expedition.enemiesDefeated()
                            + " | Supplies: 3/3 | Gold: "
                            + expedition.player().gold());
            console.line("  1  Save completed expedition    0  Finish without saving");
            while (console.choice("Finish", 0, 1) == 1) {
                if (save(expedition)) break;
            }
        } else {
            console.section("EXPEDITION LOST");
            console.line(
                    "  The island falls silent. Your last saved expedition is still available.");
            console.line("  Tip: retreat before a lethal hit, rest often, and upgrade your armor.");
        }
    }

    private void status(Expedition expedition) {
        Player player = expedition.player();
        console.section(player.name() + " / " + player.hero().label());
        int filled = (int) Math.ceil(10.0 * player.health() / player.maxHealth());
        console.line(
                "  HP ["
                        + "#".repeat(filled)
                        + ".".repeat(10 - filled)
                        + "] "
                        + player.health()
                        + "/"
                        + player.maxHealth()
                        + "   ATK "
                        + player.damage()
                        + "   BLOCK "
                        + player.armor().block()
                        + "   GOLD "
                        + player.gold());
        console.line(
                "  "
                        + player.weapon().label()
                        + " / "
                        + player.armor().label()
                        + " / Bandages "
                        + player.bandages()
                        + " / Supplies "
                        + expedition.suppliesCollected()
                        + "/3");
        if (expedition.hasAllSupplies())
            console.line("  All supplies secured. Return to the Safe House to escape!");
    }

    private void explore(Expedition expedition, Region region) {
        if (expedition.progress(region).cleared()) {
            console.line(
                    "  "
                            + region.label()
                            + " is already clear. Its "
                            + region.resource()
                            + " is in your pack.");
            return;
        }
        console.section(region.label().toUpperCase(java.util.Locale.ROOT));
        console.line("  " + region.description());
        Encounter encounter = new Encounter(expedition, region);
        while (!encounter.finished()) {
            Player player = expedition.player();
            int incoming = Math.max(0, region.enemy().damage() - player.armor().block());
            console.line(
                    "  "
                            + region.enemy().label()
                            + " HP "
                            + encounter.enemyHealth()
                            + "/"
                            + region.enemy().health()
                            + " | Your HP "
                            + player.health()
                            + "/"
                            + player.maxHealth()
                            + " | Incoming damage "
                            + incoming);
            if (incoming >= player.health() && player.damage() < encounter.enemyHealth()) {
                console.line(
                        "  DANGER: the next counterattack is lethal. Retreat or use a bandage.");
            }
            console.line(
                    "  1  Attack    2  Bandage (+10 HP, uses a turn; "
                            + player.bandages()
                            + " left)    0  Retreat");
            Encounter.Action action =
                    switch (console.choice("Action", 0, 2)) {
                        case 0 -> Encounter.Action.RETREAT;
                        case 1 -> Encounter.Action.ATTACK;
                        default -> Encounter.Action.BANDAGE;
                    };
            Encounter.Turn turn = encounter.act(action);
            if (turn.dealt() > 0) console.line("  You strike for " + turn.dealt() + " damage.");
            if (turn.healed() > 0) console.line("  Bandage restored " + turn.healed() + " HP.");
            if (turn.taken() > 0)
                console.line(
                        "  " + region.enemy().label() + " hits for " + turn.taken() + " damage.");
            switch (turn.outcome()) {
                case ENEMY_DEFEATED, REGION_CLEARED -> {
                    console.line(
                            "  "
                                    + region.enemy().label()
                                    + " defeated. +"
                                    + turn.gold()
                                    + " gold.");
                    if (turn.outcome() == Encounter.Outcome.REGION_CLEARED) {
                        console.line(
                                "  SUPPLY SECURED: "
                                        + region.resource()
                                        + ". "
                                        + expedition.suppliesCollected()
                                        + "/3 collected.");
                    } else {
                        console.line(
                                "  Another enemy approaches. You can retreat before attacking.");
                    }
                }
                case RETREATED ->
                        console.line(
                                "  You retreat safely. Defeated enemies stay defeated; the current enemy recovers.");
                case NO_BANDAGE_USED ->
                        console.line(
                                "  No bandage used: your health is full or your pack is empty. No turn spent.");
                default -> {
                    /* Health and ending messages are displayed by the surrounding loops. */
                }
            }
        }
    }

    private void shop(Player player) {
        while (true) {
            console.section("TOOL STORE | " + player.gold() + " GOLD");
            console.line("  1  Weapons    2  Armor    3  Bandage (6 gold)    0  Back to map");
            Player.Purchase purchase;
            switch (console.choice("Browse", 0, 3)) {
                case 0 -> {
                    return;
                }
                case 1 -> {
                    for (Weapon item : Weapon.values()) {
                        if (item != Weapon.FISTS)
                            console.line(
                                    "  %d  %-6s +%d ATK  %d gold"
                                            .formatted(
                                                    item.ordinal(),
                                                    item.label(),
                                                    item.damage(),
                                                    item.price()));
                    }
                    int id = console.choice("Weapon (0 cancels)", 0, Weapon.values().length - 1);
                    if (id == 0) continue;
                    purchase = player.buy(Weapon.values()[id]);
                }
                case 2 -> {
                    for (Armor item : Armor.values()) {
                        if (item != Armor.NONE)
                            console.line(
                                    "  %d  %-12s +%d BLOCK  %d gold"
                                            .formatted(
                                                    item.ordinal(),
                                                    item.label(),
                                                    item.block(),
                                                    item.price()));
                    }
                    int id = console.choice("Armor (0 cancels)", 0, Armor.values().length - 1);
                    if (id == 0) continue;
                    purchase = player.buy(Armor.values()[id]);
                }
                default -> purchase = player.buyBandage();
            }
            console.line(
                    "  "
                            + switch (purchase) {
                                case BOUGHT ->
                                        "Purchase complete. Remaining gold: " + player.gold() + ".";
                                case NOT_ENOUGH_GOLD -> "Not enough gold. Nothing was charged.";
                                case NOT_AN_UPGRADE ->
                                        "You already have equal or better equipment. Nothing was charged.";
                                case BAG_FULL ->
                                        "Your bandage pouch is full (9 maximum). Nothing was charged.";
                            });
        }
    }

    private boolean save(Expedition expedition) {
        try {
            saves.save(expedition);
            console.line("  Expedition saved. Use Continue from the main menu next time.");
            return true;
        } catch (IOException exception) {
            console.line("  Could not save: " + exception.getMessage());
            console.line(
                    "  You are still in the game. Try again, or close input to leave without saving.");
            return false;
        }
    }

    private void journal(Expedition expedition) {
        console.section("EXPEDITION JOURNAL");
        console.line(
                "  Gather all three supplies, then return to the Safe House to light a signal fire.");
        for (Region region : Region.values()) {
            var progress = expedition.progress(region);
            console.line(
                    "  ["
                            + (progress.cleared() ? "x" : " ")
                            + "] "
                            + region.resource()
                            + " / "
                            + region.label()
                            + " / "
                            + (progress.total() - progress.remaining())
                            + "/"
                            + progress.total()
                            + " enemies defeated");
        }
        console.line(
                "  Seed: "
                        + expedition.seed()
                        + " | Enemies defeated: "
                        + expedition.enemiesDefeated());
        guide();
    }

    private void guide() {
        console.section("FIELD GUIDE");
        console.line(
                "  Clear the Cave, Forest and River; bring Food, Firewood and Water home to win.");
        console.line(
                "  You attack first. A surviving enemy counters; a defeated one cannot hit back.");
        console.line("  Armor reduces each hit, down to zero. Bandages heal 10 HP and use a turn.");
        console.line(
                "  Retreat is guaranteed. Kills and gold persist, but an injured enemy heals.");
        console.line(
                "  The Safe House heals for free. Start with the Cave, and rest between enemies.");
        console.line("  Equipment replaces your current item; only upgrades can be purchased.");
        console.line("  Save from the map (7), or choose Save and quit (0). There is no autosave.");
        console.line("  Saving a new expedition replaces the previous save in the same slot.");
    }
}

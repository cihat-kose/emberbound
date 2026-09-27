package dev.emberbound.engine;

import static dev.emberbound.engine.Encounter.Action.*;
import static dev.emberbound.engine.Encounter.Outcome.*;
import static org.junit.jupiter.api.Assertions.*;

import dev.emberbound.TestFixtures;
import dev.emberbound.domain.Armor;
import dev.emberbound.domain.Expedition;
import dev.emberbound.domain.HeroClass;
import dev.emberbound.domain.Player;
import dev.emberbound.domain.Region;
import dev.emberbound.domain.Weapon;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class EncounterTest {
    @Test
    void aKilledEnemyCannotCounterattackAndPaysExactlyOnce() {
        Expedition expedition = TestFixtures.expedition(HeroClass.KNIGHT, 1);
        Encounter encounter = new Encounter(expedition, Region.CAVE);
        Encounter.Turn first = encounter.act(ATTACK);
        assertEquals(new Encounter.Turn(FIGHTING, 8, 3, 0, 0), first);
        assertEquals(2, encounter.enemyHealth());
        Encounter.Turn last = encounter.act(ATTACK);
        assertEquals(new Encounter.Turn(REGION_CLEARED, 2, 0, 0, 4), last);
        assertEquals(21, expedition.player().health());
        assertEquals(9, expedition.player().gold());
        assertTrue(encounter.finished());
        assertThrows(IllegalStateException.class, () -> encounter.act(ATTACK));
        assertThrows(IllegalStateException.class, () -> new Encounter(expedition, Region.CAVE));
    }

    @Test
    void retreatPreservesKillsButResetsAnInjuredEnemy() {
        Expedition expedition = TestFixtures.expedition(HeroClass.KNIGHT, 2);
        Encounter encounter = new Encounter(expedition, Region.CAVE);
        encounter.act(ATTACK);
        assertEquals(ENEMY_DEFEATED, encounter.act(ATTACK).outcome());
        assertEquals(10, encounter.enemyHealth());
        encounter.act(ATTACK);
        assertEquals(2, encounter.enemyHealth());
        int health = expedition.player().health();
        assertEquals(RETREATED, encounter.act(RETREAT).outcome());
        assertEquals(health, expedition.player().health());
        assertEquals(1, expedition.progress(Region.CAVE).remaining());
        assertEquals(9, expedition.player().gold());
        assertEquals(10, new Encounter(expedition, Region.CAVE).enemyHealth());
        assertThrows(IllegalStateException.class, () -> encounter.act(ATTACK));
    }

    @Test
    void deathDoesNotAwardGoldOrSupplies() {
        Player player = new Player("Ada", HeroClass.SAMURAI, 1, 15, Weapon.FISTS, Armor.NONE, 0);
        Expedition expedition = TestFixtures.expedition(player, 1);
        Encounter encounter = new Encounter(expedition, Region.RIVER);
        Encounter.Turn turn = encounter.act(ATTACK);
        assertEquals(PLAYER_DIED, turn.outcome());
        assertEquals(1, turn.taken());
        assertFalse(player.isAlive());
        assertEquals(15, player.gold());
        assertEquals(0, expedition.suppliesCollected());
        assertTrue(encounter.finished());
        assertThrows(IllegalStateException.class, () -> encounter.act(BANDAGE));
    }

    @Test
    void healingConsumesATurnAndEnemyCanCounter() {
        Player player = new Player("Ada", HeroClass.KNIGHT, 5, 5, Weapon.FISTS, Armor.NONE, 2);
        Encounter encounter = new Encounter(TestFixtures.expedition(player, 1), Region.RIVER);
        assertEquals(new Encounter.Turn(FIGHTING, 0, 7, 10, 0), encounter.act(BANDAGE));
        assertEquals(8, player.health());
        assertEquals(1, player.bandages());
        assertEquals(20, encounter.enemyHealth());
    }

    @Test
    void unavailableOrUnneededBandagesNeverCostATurn() {
        Expedition expedition = TestFixtures.expedition(HeroClass.ARCHER, 1);
        Encounter encounter = new Encounter(expedition, Region.CAVE);
        assertEquals(NO_BANDAGE_USED, encounter.act(BANDAGE).outcome());
        assertEquals(18, expedition.player().health());
        assertEquals(2, expedition.player().bandages());
        Player emptyPack = new Player("Ada", HeroClass.ARCHER, 5, 0, Weapon.FISTS, Armor.NONE, 0);
        assertEquals(
                NO_BANDAGE_USED,
                new Encounter(TestFixtures.expedition(emptyPack, 1), Region.CAVE)
                        .act(BANDAGE)
                        .outcome());
        assertEquals(5, emptyPack.health());
    }

    @Test
    void armorCanCompletelyBlockAHit() {
        Player player = new Player("Ada", HeroClass.KNIGHT, 24, 0, Weapon.FISTS, Armor.HEAVY, 0);
        Encounter encounter = new Encounter(TestFixtures.expedition(player, 1), Region.CAVE);
        assertEquals(0, encounter.act(ATTACK).taken());
        assertEquals(24, player.health());
    }

    @Test
    void aMaximumBalanceFromASaveCannotBreakAWinningTurn() {
        Player player =
                new Player(
                        "Ada", HeroClass.KNIGHT, 24, Player.MAX_GOLD, Weapon.FISTS, Armor.NONE, 2);
        Expedition expedition = TestFixtures.expedition(player, 1);
        Encounter encounter = new Encounter(expedition, Region.CAVE);
        encounter.act(ATTACK);
        Encounter.Turn result = encounter.act(ATTACK);
        assertEquals(REGION_CLEARED, result.outcome());
        assertEquals(0, result.gold());
        assertEquals(Player.MAX_GOLD, player.gold());
        assertTrue(expedition.progress(Region.CAVE).cleared());
    }

    @ParameterizedTest
    @EnumSource(HeroClass.class)
    void everyCharacterCanFinishOneHundredSeededCampaignsWithoutFarming(HeroClass hero) {
        for (long seed = 0; seed < 100; seed++) {
            Expedition expedition = Expedition.start("Survivor", hero, seed);
            if (hero == HeroClass.SAMURAI) {
                assertEquals(Player.Purchase.BOUGHT, expedition.player().buy(Armor.LIGHT));
            }
            for (Region region : Region.values()) {
                while (!expedition.progress(region).cleared()) {
                    Encounter encounter = new Encounter(expedition, region);
                    Encounter.Outcome outcome;
                    do {
                        outcome = encounter.act(ATTACK).outcome();
                        assertNotEquals(
                                PLAYER_DIED,
                                outcome,
                                hero + " died in " + region + " for seed " + seed);
                    } while (outcome == FIGHTING);
                    if (!encounter.finished()) encounter.act(RETREAT);
                    expedition.rest();
                }
            }
            assertTrue(expedition.escaped());
            assertEquals(3, expedition.suppliesCollected());
            int expectedGold =
                    hero.gold()
                            + expedition.progress().entrySet().stream()
                                    .mapToInt(
                                            entry ->
                                                    entry.getValue().total()
                                                            * entry.getKey().enemy().gold())
                                    .sum()
                            - (hero == HeroClass.SAMURAI ? Armor.LIGHT.price() : 0);
            assertEquals(expectedGold, expedition.player().gold());
        }
    }
}

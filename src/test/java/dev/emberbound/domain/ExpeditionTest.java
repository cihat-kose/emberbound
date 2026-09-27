package dev.emberbound.domain;

import static org.junit.jupiter.api.Assertions.*;

import dev.emberbound.TestFixtures;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ExpeditionTest {
    @Test
    void seedReproducesPopulationsAcrossAllRegions() {
        for (long seed = -50; seed < 50; seed++) {
            Expedition first = Expedition.start("Ada", HeroClass.ARCHER, seed);
            Expedition second = Expedition.start("Cihat", HeroClass.KNIGHT, seed);
            assertEquals(first.progress(), second.progress());
            for (var progress : first.progress().values()) {
                assertTrue(progress.total() >= 1 && progress.total() <= 3);
                assertEquals(progress.total(), progress.remaining());
            }
        }
    }

    @Test
    void winningRequiresAllSuppliesAndReturningHome() {
        Expedition expedition = TestFixtures.expedition(HeroClass.KNIGHT, 1);
        expedition.player().takeDamage(3);
        assertEquals(3, expedition.rest());
        assertFalse(expedition.escaped());
        for (Region region : Region.values()) expedition.defeatEnemy(region);
        assertTrue(expedition.hasAllSupplies());
        assertEquals(3, expedition.suppliesCollected());
        assertEquals(3, expedition.enemiesDefeated());
        assertFalse(expedition.escaped());
        expedition.rest();
        assertTrue(expedition.escaped());
        assertThrows(IllegalStateException.class, expedition::rest);
        assertThrows(IllegalStateException.class, () -> expedition.defeatEnemy(Region.CAVE));
    }

    @Test
    void rewardsCannotBeFarmedFromClearedRegions() {
        Expedition expedition = TestFixtures.expedition(HeroClass.KNIGHT, 1);
        expedition.defeatEnemy(Region.CAVE);
        assertEquals(9, expedition.player().gold());
        assertThrows(IllegalStateException.class, () -> expedition.defeatEnemy(Region.CAVE));
        assertEquals(9, expedition.player().gold());
        assertThrows(UnsupportedOperationException.class, () -> expedition.progress().clear());
    }

    @ParameterizedTest
    @CsvSource({"0, 0", "4, 2", "1, -1", "2, 3"})
    void rejectsImpossibleRegionProgress(int total, int remaining) {
        assertThrows(
                IllegalArgumentException.class, () -> new Expedition.Progress(total, remaining));
    }

    @Test
    void rejectsIncompleteOrPrematureVictorySnapshots() {
        Player player = new Player("Ada", HeroClass.KNIGHT);
        assertThrows(NullPointerException.class, () -> new Expedition(player, 0, Map.of(), false));
        var progress = TestFixtures.expedition(HeroClass.KNIGHT, 1).progress();
        assertThrows(
                IllegalArgumentException.class, () -> new Expedition(player, 0, progress, true));
        player.takeDamage(100);
        Expedition expedition = new Expedition(player, 0, progress, false);
        assertThrows(IllegalStateException.class, expedition::rest);
    }
}

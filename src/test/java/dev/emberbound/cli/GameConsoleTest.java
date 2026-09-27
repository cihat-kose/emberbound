package dev.emberbound.cli;

import static org.junit.jupiter.api.Assertions.*;

import dev.emberbound.TestFixtures;
import dev.emberbound.domain.Armor;
import dev.emberbound.domain.Expedition;
import dev.emberbound.domain.HeroClass;
import dev.emberbound.domain.Player;
import dev.emberbound.domain.Region;
import dev.emberbound.domain.Weapon;
import dev.emberbound.persistence.SaveStore;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import org.junit.jupiter.api.Test;

class GameConsoleTest {
    @Test
    void demoCompletesTheRealConsoleCampaignWithoutPersistence() {
        StringWriter output = new StringWriter();
        Demo.run(output);
        String transcript = output.toString();
        assertTrue(transcript.contains("YOU ESCAPED"));
        assertTrue(transcript.contains("Supplies: 3/3"));
        assertTrue(transcript.contains("Enemies defeated: 6"));
        assertTrue(transcript.contains("SUPPLY SECURED: Food"));
        assertTrue(transcript.contains("SUPPLY SECURED: Firewood"));
        assertTrue(transcript.contains("SUPPLY SECURED: Water"));
        assertFalse(transcript.contains("Input closed"));
        assertFalse(transcript.contains("EXPEDITION LOST"));
    }

    @Test
    void invalidMenuInputAndNamesRecoverThenSaveAndQuit() {
        MemoryStore store = new MemoryStore();
        String transcript =
                play("text\n99999999999999999999\n\n-1\n3\n1\n  \nAda\n9\n1\n6\n7\n0\n", store);
        assertTrue(transcript.contains("Please enter a number"));
        assertTrue(transcript.contains("Use a name with 1-24"));
        assertTrue(transcript.contains("FIELD GUIDE"));
        assertEquals("Ada", store.saved.player().name());
        assertEquals(HeroClass.SAMURAI, store.saved.player().hero());
        assertEquals(2, store.saveCount);
    }

    @Test
    void handlesEndOfInputAtEveryInteractiveBoundaryWithoutSaving() {
        for (String script :
                new String[] {
                    "",
                    "1\n",
                    "1\nAda\n",
                    "1\nAda\n1\n",
                    "1\nAda\n1\n3\n",
                    "1\nAda\n1\n2\n1\n",
                    "1\nAda\n1\n2\n2\n"
                }) {
            MemoryStore store = new MemoryStore();
            assertTrue(play(script, store).contains("Input closed"));
            assertEquals(0, store.saveCount);
        }
    }

    @Test
    void failedLoadingReturnsToMenuAndNeverOverwritesTheSlot() {
        MemoryStore store = new MemoryStore();
        String transcript = play("2\n0\n", store);
        assertTrue(transcript.contains("Could not load"));
        assertEquals(0, store.saveCount);
    }

    @Test
    void failedSaveAndQuitKeepsTheGamePlayable() {
        MemoryStore store = new MemoryStore();
        store.failSaving = true;
        String transcript = play("1\nAda\n2\n0\n6\n", store);
        assertTrue(transcript.contains("Could not save"));
        assertTrue(transcript.contains("EXPEDITION JOURNAL"));
        assertTrue(transcript.contains("Input closed"));
        assertNull(store.saved);
    }

    @Test
    void shopSupportsUpgradesCancellationsAndRejectedPurchases() {
        MemoryStore store = new MemoryStore();
        store.saved =
                TestFixtures.expedition(
                        new Player("Ada", HeroClass.KNIGHT, 24, 150, Weapon.FISTS, Armor.NONE, 8),
                        1);
        String transcript = play("2\n2\n1\n0\n2\n0\n1\n3\n1\n1\n2\n3\n2\n1\n3\n3\n0\n0\n", store);
        assertEquals(Weapon.RIFLE, store.saved.player().weapon());
        assertEquals(Armor.HEAVY, store.saved.player().armor());
        assertEquals(9, store.saved.player().bandages());
        assertEquals(59, store.saved.player().gold());
        assertTrue(transcript.contains("equal or better equipment"));
        assertTrue(transcript.contains("pouch is full"));
        assertFalse(transcript.contains("Input closed"));
    }

    @Test
    void unaffordablePurchaseDoesNotSpendMoney() {
        MemoryStore store = new MemoryStore();
        String transcript = play("1\nAda\n3\n2\n1\n3\n2\n3\n3\n0\n0\n", store);
        assertEquals(5, store.saved.player().gold());
        assertTrue(transcript.contains("Not enough gold"));
    }

    @Test
    void deathPrintsAnEndingAndDoesNotReplacePreviousSave() {
        MemoryStore store = new MemoryStore();
        store.saved =
                TestFixtures.expedition(
                        new Player("Ada", HeroClass.ARCHER, 1, 20, Weapon.FISTS, Armor.NONE, 0), 1);
        String transcript = play("2\n5\n2\n1\n", store);
        assertTrue(transcript.contains("DANGER"));
        assertTrue(transcript.contains("No bandage used"));
        assertTrue(transcript.contains("EXPEDITION LOST"));
        assertEquals(0, store.saveCount);
    }

    @Test
    void clearedRegionsCannotBeReenteredAndCompletedRunCanBeSaved() {
        MemoryStore store = new MemoryStore();
        store.saved = TestFixtures.expedition(HeroClass.KNIGHT, 1);
        for (Region region : Region.values()) store.saved.defeatEnemy(region);
        String transcript = play("2\n3\n1\n1\n", store);
        assertTrue(transcript.contains("already clear"));
        assertTrue(transcript.contains("YOU ESCAPED"));
        assertEquals(1, store.saveCount);
        assertTrue(store.saved.escaped());
    }

    @Test
    void completedSaveDisplaysTheEndingAgain() {
        MemoryStore store = new MemoryStore();
        store.saved = TestFixtures.expedition(HeroClass.KNIGHT, 1);
        for (Region region : Region.values()) store.saved.defeatEnemy(region);
        store.saved.rest();
        assertTrue(play("2\n0\n", store).contains("YOU ESCAPED"));
    }

    @Test
    void bandageHealingAndSafeRetreatAreVisibleToThePlayer() {
        MemoryStore store = new MemoryStore();
        store.saved =
                TestFixtures.expedition(
                        new Player("Ada", HeroClass.KNIGHT, 5, 5, Weapon.FISTS, Armor.NONE, 2), 1);
        String transcript = play("2\n5\n2\n0\n1\n0\n", store);
        assertTrue(transcript.contains("Bandage restored 10 HP"));
        assertTrue(transcript.contains("You retreat safely"));
        assertEquals(24, store.saved.player().health());
    }

    private String play(String script, SaveStore store) {
        StringWriter output = new StringWriter();
        new GameConsole(new Console(new StringReader(script), output), store, 7L).run();
        return output.toString();
    }

    private static final class MemoryStore implements SaveStore {
        private Expedition saved;
        private boolean failSaving;
        private int saveCount;

        @Override
        public Expedition load() throws IOException {
            if (saved == null) throw new IOException("No save exists.");
            return saved;
        }

        @Override
        public void save(Expedition expedition) throws IOException {
            if (failSaving) throw new IOException("Storage unavailable.");
            saved = expedition;
            saveCount++;
        }
    }
}

package dev.emberbound.persistence;

import static org.junit.jupiter.api.Assertions.*;

import dev.emberbound.TestFixtures;
import dev.emberbound.domain.Armor;
import dev.emberbound.domain.Expedition;
import dev.emberbound.domain.HeroClass;
import dev.emberbound.domain.Region;
import dev.emberbound.engine.Encounter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FileSaveStoreTest {
    @TempDir Path temporary;

    @Test
    void roundTripPreservesUnicodeInventoryHealthSeedAndPartialProgress() throws IOException {
        Path path = temporary.resolve("slots/one.properties");
        FileSaveStore store = new FileSaveStore(path);
        Expedition original = Expedition.start("Cihat Köse", HeroClass.SAMURAI, 7);
        original.player().buy(Armor.LIGHT);
        Encounter encounter = new Encounter(original, Region.CAVE);
        encounter.act(Encounter.Action.ATTACK);
        encounter.act(Encounter.Action.ATTACK);
        encounter.act(Encounter.Action.RETREAT);
        original.player().useBandage();
        store.save(original);

        Expedition loaded = store.load();
        assertAll(
                () -> assertEquals(original.player().name(), loaded.player().name()),
                () -> assertEquals(original.player().hero(), loaded.player().hero()),
                () -> assertEquals(original.player().health(), loaded.player().health()),
                () -> assertEquals(original.player().gold(), loaded.player().gold()),
                () -> assertEquals(original.player().armor(), loaded.player().armor()),
                () -> assertEquals(original.player().weapon(), loaded.player().weapon()),
                () -> assertEquals(original.player().bandages(), loaded.player().bandages()),
                () -> assertEquals(original.seed(), loaded.seed()),
                () -> assertEquals(original.progress(), loaded.progress()),
                () -> assertFalse(loaded.escaped()));
    }

    @Test
    void savesCompletedExpeditionsAndReplacesOldCheckpoints() throws IOException {
        FileSaveStore store = new FileSaveStore(temporary.resolve("save.properties"));
        Expedition expedition = TestFixtures.expedition(HeroClass.KNIGHT, 1);
        store.save(expedition);
        for (Region region : Region.values()) expedition.defeatEnemy(region);
        expedition.rest();
        store.save(expedition);
        assertTrue(store.load().escaped());
        assertEquals(3, store.load().enemiesDefeated());
        try (var files = Files.list(temporary)) {
            assertEquals(1, files.count(), "Temporary files must be cleaned up.");
        }
    }

    @ParameterizedTest
    @CsvSource({
        "version, 99",
        "hero, WIZARD",
        "health, 0",
        "health, -1",
        "health, 999",
        "gold, -1",
        "gold, 2147483648",
        "gold, abc",
        "bandages, 10",
        "weapon, LASER",
        "armor, GLASS",
        "seed, invalid",
        "escaped, perhaps",
        "escaped, true",
        "CAVE.total, 0",
        "FOREST.total, 4",
        "RIVER.remaining, 4",
        "CAVE.remaining, -1"
    })
    void rejectsCorruptedFieldsWithoutChangingTheSave(String key, String value) throws IOException {
        Path path = temporary.resolve("save.properties");
        FileSaveStore store = new FileSaveStore(path);
        store.save(TestFixtures.expedition(HeroClass.KNIGHT, 1));
        Properties properties = readProperties(path);
        properties.setProperty(key, value);
        writeProperties(path, properties);
        byte[] before = Files.readAllBytes(path);
        assertThrows(IOException.class, store::load);
        assertArrayEquals(before, Files.readAllBytes(path));
    }

    @Test
    void rejectsMissingFieldsBrokenEscapesAndOversizedFiles() throws IOException {
        Path path = temporary.resolve("save.properties");
        FileSaveStore store = new FileSaveStore(path);
        store.save(TestFixtures.expedition(HeroClass.ARCHER, 1));
        Properties properties = readProperties(path);
        properties.remove("FOREST.remaining");
        writeProperties(path, properties);
        assertThrows(IOException.class, store::load);
        Files.writeString(path, "name=\\uNOTHEX\n");
        assertThrows(IOException.class, store::load);
        Files.writeString(path, "x".repeat(16_385));
        assertTrue(assertThrows(IOException.class, store::load).getMessage().contains("too large"));
    }

    @Test
    void missingSaveIsReportedWithoutCreatingFiles() {
        Path path = temporary.resolve("missing.properties");
        assertThrows(IOException.class, new FileSaveStore(path)::load);
        assertFalse(Files.exists(path));
    }

    @Test
    void aDeadRunCannotOverwriteALiveCheckpoint() throws IOException {
        Path path = temporary.resolve("save.properties");
        FileSaveStore store = new FileSaveStore(path);
        Expedition expedition = TestFixtures.expedition(HeroClass.ARCHER, 1);
        store.save(expedition);
        byte[] before = Files.readAllBytes(path);
        expedition.player().takeDamage(100);
        assertThrows(IOException.class, () -> store.save(expedition));
        assertArrayEquals(before, Files.readAllBytes(path));
    }

    @Test
    void failedReplacementPreservesTheTargetAndCleansItsTemporaryFile() throws IOException {
        Path directory = Files.createDirectory(temporary.resolve("occupied"));
        Path marker = Files.writeString(directory.resolve("keep.txt"), "existing content");
        FileSaveStore store = new FileSaveStore(directory);
        assertThrows(
                IOException.class, () -> store.save(TestFixtures.expedition(HeroClass.KNIGHT, 1)));
        assertEquals("existing content", Files.readString(marker));
        try (var files = Files.list(temporary)) {
            assertEquals(1, files.count());
        }
    }

    private Properties readProperties(Path path) throws IOException {
        Properties properties = new Properties();
        try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            properties.load(reader);
        }
        return properties;
    }

    private void writeProperties(Path path, Properties properties) throws IOException {
        try (var writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            properties.store(writer, "test save");
        }
    }
}

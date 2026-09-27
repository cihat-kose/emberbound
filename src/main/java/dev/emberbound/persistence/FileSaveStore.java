package dev.emberbound.persistence;

import dev.emberbound.domain.Armor;
import dev.emberbound.domain.Expedition;
import dev.emberbound.domain.HeroClass;
import dev.emberbound.domain.Player;
import dev.emberbound.domain.Region;
import dev.emberbound.domain.Weapon;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/** Versioned, bounded text saves. Parse and validate everything before exposing a loaded game. */
public final class FileSaveStore implements SaveStore {
    private static final int MAX_SAVE_BYTES = 16_384;
    private final Path path;

    public FileSaveStore(Path path) {
        this.path = Objects.requireNonNull(path).toAbsolutePath().normalize();
    }

    @Override
    public Expedition load() throws IOException {
        try (var input = Files.newInputStream(path)) {
            byte[] bytes = input.readNBytes(MAX_SAVE_BYTES + 1);
            if (bytes.length > MAX_SAVE_BYTES) throw new IOException("Save file is too large.");
            Properties data = new Properties();
            data.load(new StringReader(new String(bytes, StandardCharsets.UTF_8)));
            if (!"1".equals(required(data, "version"))) {
                throw new IOException("Unsupported save version. Expected version 1.");
            }
            Player player =
                    new Player(
                            required(data, "name"),
                            HeroClass.valueOf(required(data, "hero")),
                            integer(data, "health"),
                            integer(data, "gold"),
                            Weapon.valueOf(required(data, "weapon")),
                            Armor.valueOf(required(data, "armor")),
                            integer(data, "bandages"));
            if (!player.isAlive())
                throw new IllegalArgumentException("A defeated expedition cannot be resumed.");
            Map<Region, Expedition.Progress> progress = new EnumMap<>(Region.class);
            for (Region region : Region.values()) {
                progress.put(
                        region,
                        new Expedition.Progress(
                                integer(data, region.name() + ".total"),
                                integer(data, region.name() + ".remaining")));
            }
            String escaped = required(data, "escaped");
            if (!escaped.equals("true") && !escaped.equals("false")) {
                throw new IllegalArgumentException("Invalid escape status.");
            }
            return new Expedition(
                    player,
                    Long.parseLong(required(data, "seed")),
                    progress,
                    Boolean.parseBoolean(escaped));
        } catch (IllegalArgumentException exception) {
            throw new IOException("Save file is invalid: " + exception.getMessage(), exception);
        }
    }

    @Override
    public void save(Expedition expedition) throws IOException {
        Objects.requireNonNull(expedition);
        Player player = expedition.player();
        if (!player.isAlive()) throw new IOException("A defeated expedition cannot be saved.");
        Properties data = new Properties();
        data.setProperty("version", "1");
        data.setProperty("name", player.name());
        data.setProperty("hero", player.hero().name());
        data.setProperty("health", Integer.toString(player.health()));
        data.setProperty("gold", Integer.toString(player.gold()));
        data.setProperty("weapon", player.weapon().name());
        data.setProperty("armor", player.armor().name());
        data.setProperty("bandages", Integer.toString(player.bandages()));
        data.setProperty("seed", Long.toString(expedition.seed()));
        data.setProperty("escaped", Boolean.toString(expedition.escaped()));
        for (Region region : Region.values()) {
            data.setProperty(
                    region.name() + ".total",
                    Integer.toString(expedition.progress(region).total()));
            data.setProperty(
                    region.name() + ".remaining",
                    Integer.toString(expedition.progress(region).remaining()));
        }

        Path parent = path.getParent();
        Files.createDirectories(parent);
        Path temporary = Files.createTempFile(parent, ".emberbound-", ".tmp");
        try {
            try (var output = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) {
                data.store(output, "Emberbound save - schema version 1");
            }
            try {
                Files.move(
                        temporary,
                        path,
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private static String required(Properties data, String key) {
        String value = data.getProperty(key);
        if (value == null) throw new IllegalArgumentException("Missing field: " + key);
        return value;
    }

    private static int integer(Properties data, String key) {
        return Integer.parseInt(required(data, key));
    }
}

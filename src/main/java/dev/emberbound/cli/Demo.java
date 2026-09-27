package dev.emberbound.cli;

import dev.emberbound.domain.Expedition;
import dev.emberbound.domain.HeroClass;
import dev.emberbound.domain.Region;
import dev.emberbound.persistence.SaveStore;
import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;

/** Plays the real console/engine through a complete expedition, without touching save files. */
public final class Demo {
    public static final long SEED = 7L;

    private Demo() {}

    public static String winningScript() {
        Expedition expedition = Expedition.start("Ada", HeroClass.KNIGHT, SEED);
        StringBuilder script = new StringBuilder("1\nAda\n3\n6\n");
        for (Region region : Region.values()) {
            int enemies = expedition.progress(region).total();
            int attacks =
                    (region.enemy().health() + HeroClass.KNIGHT.damage() - 1)
                            / HeroClass.KNIGHT.damage();
            for (int enemy = 0; enemy < enemies; enemy++) {
                script.append(region.ordinal() + 3).append('\n');
                script.append("1\n".repeat(attacks));
                if (enemy + 1 < enemies) script.append("0\n");
                script.append("1\n");
            }
        }
        return script.append("0\n").toString();
    }

    public static void run(Writer output) {
        SaveStore noSaves =
                new SaveStore() {
                    @Override
                    public Expedition load() throws IOException {
                        throw new IOException("Demo does not use saves.");
                    }

                    @Override
                    public void save(Expedition expedition) throws IOException {
                        throw new IOException("Demo does not use saves.");
                    }
                };
        Console console = new Console(new StringReader(winningScript()), output, true);
        console.line("  WATCH MODE / deterministic demo / no save files are read or written");
        new GameConsole(console, noSaves, SEED).run();
    }
}

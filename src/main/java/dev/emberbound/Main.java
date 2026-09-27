package dev.emberbound;

import dev.emberbound.cli.Console;
import dev.emberbound.cli.Demo;
import dev.emberbound.cli.GameConsole;
import dev.emberbound.persistence.FileSaveStore;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public final class Main {
    private Main() {}

    public static void main(String[] args) {
        int result =
                run(
                        args,
                        new InputStreamReader(System.in, StandardCharsets.UTF_8),
                        new OutputStreamWriter(System.out, StandardCharsets.UTF_8));
        if (result != 0) System.exit(result);
    }

    public static int run(String[] args, Reader input, Writer output) {
        PrintWriter messages = new PrintWriter(output, true);
        try {
            long seed = System.nanoTime();
            Path savePath = Path.of(".saves", "expedition.properties");
            boolean demo = false;
            boolean help = false;
            for (int index = 0; index < args.length; index++) {
                switch (args[index]) {
                    case "--help", "-h" -> help = true;
                    case "--demo" -> demo = true;
                    case "--seed" -> {
                        if (++index == args.length)
                            throw new IllegalArgumentException("--seed needs an integer.");
                        seed = Long.parseLong(args[index]);
                    }
                    case "--save" -> {
                        if (++index == args.length
                                || args[index].isBlank()
                                || args[index].startsWith("--")) {
                            throw new IllegalArgumentException("--save needs a file path.");
                        }
                        savePath = Path.of(args[index]);
                    }
                    default -> throw new IllegalArgumentException("Unknown option: " + args[index]);
                }
            }
            if (help) {
                messages.println("Emberbound | Java Adventure Game");
                messages.println(
                        "Usage: java -jar target/emberbound.jar [--seed INTEGER] [--save FILE] [--demo] [--help]");
                messages.println(
                        "  --seed  Repeat the same island populations for a new expedition.");
                messages.println(
                        "  --save  Save slot (default: .saves/expedition.properties, relative to working directory).");
                messages.println(
                        "  --demo  Watch a complete, fixed-seed expedition; never reads or writes saves.");
                messages.println("Requires Java 21 or newer. No runtime dependencies.");
                return 0;
            }
            if (demo) {
                Demo.run(output);
            } else {
                new GameConsole(new Console(input, output), new FileSaveStore(savePath), seed)
                        .run();
            }
            return 0;
        } catch (IllegalArgumentException exception) {
            messages.println("Cannot start: " + exception.getMessage());
            messages.println("Use --help for available options.");
            return 2;
        } catch (UncheckedIOException exception) {
            messages.println("Terminal error: " + exception.getMessage());
            return 1;
        }
    }
}

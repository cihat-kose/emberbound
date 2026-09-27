package dev.emberbound;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class MainTest {
    @TempDir Path temporary;

    @ParameterizedTest
    @ValueSource(
            strings = {
                "--unknown",
                "--seed",
                "--seed not-a-number",
                "--seed 99999999999999999999",
                "--save"
            })
    void malformedOptionsHaveAnActionableMessageAndUsageExitCode(String arguments) {
        StringWriter output = new StringWriter();
        assertEquals(2, Main.run(arguments.split(" "), new StringReader(""), output));
        assertTrue(output.toString().contains("Use --help"));
    }

    @Test
    void aSaveOptionCannotConsumeTheNextFlagOrABlankValue() {
        for (String value : new String[] {"--demo", " "}) {
            StringWriter output = new StringWriter();
            assertEquals(2, Main.run(new String[] {"--save", value}, new StringReader(""), output));
            assertTrue(output.toString().contains("--save needs a file path"));
        }
    }

    @Test
    void helpDoesNotStartAGame() {
        StringWriter output = new StringWriter();
        assertEquals(0, Main.run(new String[] {"--help"}, new StringReader(""), output));
        assertTrue(output.toString().contains("Usage:"));
        assertFalse(output.toString().contains("MAIN MENU"));
    }

    @Test
    void demoNeverTouchesAnExplicitSavePath() {
        Path path = temporary.resolve("leave-absent.properties");
        StringWriter output = new StringWriter();
        assertEquals(
                0,
                Main.run(
                        new String[] {"--demo", "--save", path.toString()},
                        new StringReader(""),
                        output));
        assertTrue(output.toString().contains("YOU ESCAPED"));
        assertFalse(Files.exists(path));
    }

    @Test
    void commandLineSeedAndSavePathAreUsedAndCanBeResumed() throws IOException {
        Path path = temporary.resolve("slot with spaces.properties");
        String[] arguments = {"--seed", "-123", "--save", path.toString()};
        StringWriter output = new StringWriter();
        assertEquals(0, Main.run(arguments, new StringReader("1\nCihat\n2\n0\n"), output));
        assertTrue(Files.readString(path).contains("seed=-123"));
        assertTrue(output.toString().contains("Expedition saved"));
        StringWriter restored = new StringWriter();
        assertEquals(0, Main.run(arguments, new StringReader("2\n6\n0\n"), restored));
        assertTrue(restored.toString().contains("Welcome back, Cihat"));
        assertTrue(restored.toString().contains("Seed: -123"));
    }

    @Test
    void unreadableTerminalHasANonzeroExitCode() {
        Reader broken =
                new Reader() {
                    @Override
                    public int read(char[] buffer, int offset, int length) throws IOException {
                        throw new IOException("Disconnected terminal");
                    }

                    @Override
                    public void close() {}
                };
        StringWriter output = new StringWriter();
        assertEquals(1, Main.run(new String[0], broken, output));
        assertTrue(output.toString().contains("Terminal error"));
    }
}

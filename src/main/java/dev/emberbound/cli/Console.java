package dev.emberbound.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;

/** One line-oriented input owner for the entire application; injected streams stay open. */
public final class Console {
    public static final class EndOfInput extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }

    private final BufferedReader input;
    private final PrintWriter output;
    private final boolean echo;

    public Console(Reader input, Writer output) {
        this(input, output, false);
    }

    public Console(Reader input, Writer output, boolean echo) {
        this.input = new BufferedReader(input);
        this.output = new PrintWriter(output, true);
        this.echo = echo;
    }

    public void line(String text) {
        output.println(text);
    }

    public void section(String title) {
        line("");
        line("  --- " + title + " ---");
    }

    public String read(String prompt) {
        output.print("  " + prompt + " > ");
        output.flush();
        try {
            String value = input.readLine();
            if (value == null) throw new EndOfInput();
            if (echo) output.println(value);
            return value;
        } catch (IOException exception) {
            throw new UncheckedIOException("Could not read terminal input.", exception);
        }
    }

    public int choice(String prompt, int minimum, int maximum) {
        while (true) {
            try {
                int selection = Integer.parseInt(read(prompt).strip());
                if (selection >= minimum && selection <= maximum) return selection;
            } catch (NumberFormatException ignored) {
                // The next prompt consumes a new line, including after overflow or empty input.
            }
            line("  Please enter a number from " + minimum + " to " + maximum + ".");
        }
    }
}

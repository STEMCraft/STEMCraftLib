package com.stemcraft.exception;

import com.stemcraft.STEMCraftPlugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;


public class STEMCraftException extends RuntimeException {
    private static final STEMCraftPlugin plugin = JavaPlugin.getPlugin(STEMCraftPlugin.class);

    /**
     * Create a new exception.
     */
    public STEMCraftException() {
        log(this);
    }

    /**
     * Create a new exception.
     * @param t The exception throwable
     */
    public STEMCraftException(Throwable t) {
        super(t);
        log(t);
    }

    /**
     * Create a new exception.
     * @param message The exception message
     */
    public STEMCraftException(String message) {
        super(message);
        log(this, message);
    }

    /**
     * Create a new exception.
     * @param message The exception message
     * @param t The exception throwable
     */
    public STEMCraftException(String message, Throwable t) {
        super(message, t);
        log(t, message);
    }

    /**
     * Print an error to the server console
     * @param t The throwable to print
     * @param messages The messages to print
     */
    public void log(Throwable t, String... messages) {
        try {
            final List<String> lines = new ArrayList<>();

            //noinspection deprecation
            lines.add(plugin.getDescription().getName() + " " + plugin.getDescription().getVersion() + " encountered a series error");
            if(messages != null) {
                lines.addAll(Arrays.asList(messages));
            }

            // Get stack trace
            lines.addAll(getStackTrace(t));

            // Log to the console
            plugin.getLogger().log(Level.SEVERE, String.join("\n", lines));

        } catch (final Throwable secondError) {
            plugin.getLogger().log(Level.SEVERE, "Got error when saving another error!");
        }
    }

    /**
     * Generate the stack trace of the throwable
     * @param t The throwable
     * @return The stack trace
     */
    public static List<String> getStackTrace(Throwable t) {
        List<String> lines = new ArrayList<>();

        do {
            // Write the error header
            if(t == null) {
                lines.add("Unknown error");
                break;
            } else {
                String v = t.getMessage();
                if(v == null || v.isBlank()) {
                    v = t.getLocalizedMessage();
                    if(v == null || v.isBlank()) {
                        v = "(Unknown cause)";
                    }
                }

                lines.add(t.getClass().getSimpleName() + " " + v);
            }

            int count = 0;

            for (final StackTraceElement el : t.getStackTrace()) {
                count++;

                final String trace = el.toString();

                if (trace.contains("sun.reflect"))
                    continue;

                if (count > 6 && trace.startsWith("net.minecraft.server"))
                    break;

                lines.add("\t at " + el);
            }
        } while ((t = t.getCause()) != null);

        return lines;
    }
}


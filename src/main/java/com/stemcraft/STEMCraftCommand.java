package com.stemcraft;

import com.stemcraft.util.SCTabCompletion;
import org.bukkit.command.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class STEMCraftCommand implements TabExecutor {
    private final List<String[]> tabCompletionList = new ArrayList<>();

    /**
     * Add a tab completion list to the command
     * @param args The array of tab completion items
     */
    public void addTabCompletion(String... args) {
        tabCompletionList.add(args);
    }

    /**
     * Called when the console or player executes the command
     * @param sender The sender of the command.
     * @param command The command name.
     * @param args The command arguments.
     */
    public void execute(CommandSender sender, String command, List<String> args) {
        // empty
    }

    /**
     * Display a message to the command sender
     * @param sender The command sender.
     * @param message The message to send.
     * @param args The message placeholders to replace.
     */
    public void message(CommandSender sender, String message, String... args) {
        STEMCraftLib.message(sender, message, args);
    }

    /**
     * Display a info message to the command sender
     * @param sender The command sender.
     * @param message The message to send.
     * @param args The message placeholders to replace.
     */
    public void info(CommandSender sender, String message, String... args) {
        STEMCraftLib.info(sender, message, args);
    }

    /**
     * Display a warning message to the command sender
     * @param sender The command sender.
     * @param message The message to send.
     * @param args The message placeholders to replace.
     */
    public void warning(CommandSender sender, String message, String... args) {
        STEMCraftLib.warning(sender, message, args);
    }

    /**
     * Display a error message to the command sender
     * @param sender The command sender.
     * @param message The message to send.
     * @param args The message placeholders to replace.
     */
    public void error(CommandSender sender, String message, String... args) {
        STEMCraftLib.error(sender, message, args);
    }

    /**
     * Display a success message to the command sender
     * @param sender The command sender.
     * @param message The message to send.
     * @param args The message placeholders to replace.
     */
    public void success(CommandSender sender, String message, String... args) {
        STEMCraftLib.success(sender, message, args);
    }

    /**
     * The Bukkit onCommand method
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        execute(sender, label, Arrays.stream(args).toList());
        return true;
    }

    private static class TabCompleteValueOption {
        String option;
        String value;

        TabCompleteValueOption(String option, String value) {
            this.option = option;
            this.value = value;
        }
    }

    private static class TabCompleteArgParser {
        List<String> optionArgsAvailable = new ArrayList<>();
        Map<String, List<String>> valueOptionArgsAvailable = new HashMap<>();
        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection") // future usage
        List<String> optionArgsUsed = new ArrayList<>();
        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection") // future usage
        List<String> valueOptionArgsUsed = new ArrayList<>();
        Integer argIndex = 0;
        String[] args;

        public TabCompleteArgParser(String[] args) {
            this.args = args;
        }

        public String getStringAsOption(String arg) {
            if (arg.startsWith("-")) {
                return arg.toLowerCase();
            }

            return null;
        }

        public void addOption(String option) {
            optionArgsAvailable.add(option);
        }

        public TabCompleteValueOption getStringAsValueOption(String arg) {
            if (arg.matches("^[a-zA-Z0-9-_]:.*")) {
                String option = arg.substring(0, arg.indexOf(':')).toLowerCase();
                String value = arg.substring(arg.indexOf(':') + 1);

                return new TabCompleteValueOption(option, value);
            }

            return null;
        }

        public void addValueOption(TabCompleteValueOption option) {
            valueOptionArgsAvailable.put(option.option, parseValue(option.value));
        }

        public List<String> parseValue(String value) {
            List<String> list = new ArrayList<>();

            if (value.startsWith("{") && value.endsWith("}")) {
                String placeholder = value.substring(1, value.length() - 1);
                List<String> placeholderList = SCTabCompletion.list(placeholder);
                list.addAll(placeholderList);
            } else {
                list.add(value);
            }

            return list;
        }


        public Boolean hasRemainingArgs() {
            return argIndex < args.length - 1;
        }

        public void next() {
            nextMatches(null);
        }

        public Boolean nextMatches(String tabCompletionItem) {
            for (; argIndex < args.length; argIndex++) {
                String arg = args[argIndex];

                String option = getStringAsOption(arg);
                if (option != null) {
                    optionArgsUsed.add(option);
                    optionArgsAvailable.remove(option);
                    continue;
                }

                TabCompleteValueOption valueOption = getStringAsValueOption(arg);
                if (valueOption != null) {
                    valueOptionArgsUsed.add(valueOption.option);
                    valueOptionArgsAvailable.remove(valueOption.option);
                    continue;
                }

                if (tabCompletionItem == null) {
                    argIndex++;
                    return true;
                }

                List<String> values = parseValue(tabCompletionItem);
                if (values.contains(arg)) {
                    argIndex++;
                    return true;
                }

                return false;
            }

            // To get here we are out of args to parse
            return null;
        }

        public void processRemainingArgs() {
            while (hasRemainingArgs()) {
                next();
            }
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        List<String> tabCompletionResults = new ArrayList<>();
        List<String> optionArgsAvailable = new ArrayList<>();
        Map<String, List<String>> valueOptionArgsAvailable = new HashMap<>();
        String[] fullArgs = new String[args.length - 1];

        System.arraycopy(args, 0, fullArgs, 0, args.length - 1);

        // iterate each tab completion list
        tabCompletionList.forEach(list -> {
            boolean matches = true;
            int listIndex;

            // Copy the elements except the last one
            TabCompleteArgParser argParser = new TabCompleteArgParser(fullArgs);

            // iterate each tab completion list item
            for (listIndex = 0; listIndex < list.length; listIndex++) {
                String listItem = list[listIndex];

                // list item is an option
                String option = argParser.getStringAsOption(listItem);
                if (option != null) {
                    argParser.addOption(option);
                    continue;
                }

                // list item is a value option
                TabCompleteValueOption valueOption = argParser.getStringAsValueOption(listItem);
                if (valueOption != null) {
                    argParser.addValueOption(valueOption);
                    continue;
                }

                // list item is a string or placeholder
                Boolean nextMatches = argParser.nextMatches(listItem);
                if (nextMatches == null) {
                    tabCompletionResults.addAll(argParser.parseValue(listItem));
                    break;
                } else if (!nextMatches) {
                    matches = false;
                    break;
                }
            }

            if (matches) {
                // parse remaining arg items
                argParser.processRemainingArgs();

                optionArgsAvailable.addAll(argParser.optionArgsAvailable);
                valueOptionArgsAvailable.putAll(argParser.valueOptionArgsAvailable);
            }
        });

        // remove non-matching items from the results based on what the player has already entered
        if (!args[args.length - 1].isEmpty()) {
            String arg = args[args.length - 1];

            // if the player has only a dash in the arg, only show dash arguments
            if (arg.equals("-")) {
                return optionArgsAvailable;
            }

            // if the player has written the start of a option arg
            if (arg.contains(":")) {
                // if the option arg is available
                String key = arg.substring(0, arg.indexOf(":"));
                if (valueOptionArgsAvailable.containsKey(key)) {
                    tabCompletionResults.clear();
                    String prefix = key + ":";
                    for (String item : valueOptionArgsAvailable.get(key)) {
                        tabCompletionResults.add(prefix + item);
                    }
                }
            }

            // remove items in tabCompletionResults that do not contain the current arg text

            tabCompletionResults.removeIf(item -> !item.contains(arg));
        }

        return tabCompletionResults;
    }

    /**
     * Write a debug message to the console and operator players.
     * @param message The debug message
     */
    public void debug(String message) {
        STEMCraftLib.debug(message);
    }
}

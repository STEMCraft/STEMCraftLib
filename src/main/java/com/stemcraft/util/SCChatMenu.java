package com.stemcraft.util;

import com.stemcraft.STEMCraftLib;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.function.BiFunction;

public class SCChatMenu {
    private final static int ITEMS_PER_PAGE = 8;

    /**
     * Render a Chat Menu for the player
     * @param sender The sender requesting the menu
     * @param title The menu title
     * @param command The command to show the menu. Page numbers will be appended
     * @param page The current page
     * @param count The number of items in the menu
     * @param func The callback function to display each item in the menu
     * @param noneText The string to display when no items are found
     */
    public static void render(CommandSender sender, String title, String command, int page, int count, BiFunction<Integer, Integer, List<Component>> func, String noneText) {
        int start = (page - 1) * ITEMS_PER_PAGE;
        int maxPages = (int)Math.ceil((double) count / ITEMS_PER_PAGE);
        List<Component> lines = func.apply(start, ITEMS_PER_PAGE);

        if(lines.isEmpty()) {
            STEMCraftLib.error(sender, noneText);
        }

        sender.sendMessage(createSeparatorString(Component.text(title, NamedTextColor.AQUA)));

        // Display the content for the current page
        for (Component line : lines) {
            sender.sendMessage(line);
        }

        // Pagination
        Component prev = Component.text("<<< ", (page <= 1 ? NamedTextColor.GRAY : NamedTextColor.GOLD));
        if(page > 1) {
            prev = prev.clickEvent(ClickEvent.runCommand(command + " " + (page - 1)))
                    .hoverEvent(HoverEvent.showText(Component.text("Previous page")));
        }

        Component pageInfo = Component.text("page ", NamedTextColor.YELLOW)
                .append(Component.text(page, NamedTextColor.GOLD)
                        .append(Component.text(" of " + maxPages, NamedTextColor.YELLOW)));

        Component next = Component.text(" >>>", (page >= maxPages ? NamedTextColor.GRAY : NamedTextColor.GOLD));
        if(page < maxPages) {
            next = next.clickEvent(ClickEvent.runCommand(command + " " + (page + 1)))
                    .hoverEvent(HoverEvent.showText(Component.text("Next page")));
        }

        sender.sendMessage(createSeparatorString(prev.append(Component.space()).append(pageInfo).append(Component.space()).append(next)));
    }

    /**
     * Generates the dash line texts with text centered ie ------ TITLE --------
     * @param title The component to centre
     * @return The resulting component
     */
    private static Component createSeparatorString(Component title) {
        String separator = "-";

        int maxLength = 58;
        int titleLength = title.toString().length();
        int separatorLength = (maxLength - titleLength - 4) / 2;

        String separatorStr = separator.repeat(separatorLength);

        return Component.text(separatorStr + " ", NamedTextColor.YELLOW)
                .append(title)
                .append(Component.text(" " + separatorStr, NamedTextColor.YELLOW));
    }

    /**
     * Get the page number requested from command args
     * @param args The command args
     * @param index Which command arg contains the page number
     * @param defaultPage The default page number to use if no page number is in args
     * @return The page number
     */
    public static int getPageFromArgs(List<String> args, int index, int defaultPage) {
        if (args != null && !args.isEmpty()) {
            try {
                int p = Integer.parseInt(args.get(index));
                if(p >= 1) {
                    return p;
                }
            } catch (NumberFormatException e) {
                // empty
            }
        }

        return defaultPage;
    }

    public static int getPageFromArgs(List<String> args) {
        return getPageFromArgs(args, 0, 1);
    }
}
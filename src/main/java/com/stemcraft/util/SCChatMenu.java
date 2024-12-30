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
    private CommandSender sender;
    private int page;
    private int count;
    private String command;
    private String title;
    private String none = "No items where found";
    private final static int ITEMS_PER_PAGE = 8;

    public SCChatMenu(CommandSender sender, int page) {
        this.sender = sender;
        this.page = page;
    }

    public SCChatMenu(CommandSender sender, String page) {
        try {
            this.page = Integer.parseInt(page);
        } catch(Exception e) {
            this.page = 0;
        }
    }

    public SCChatMenu count(int count) {
        this.count = count;
        return this;
    }

    public SCChatMenu command(String command) {
        this.command = command;
        return this;
    }

    public SCChatMenu title(String title) {
        this.title = title;
        return this;
    }

    /**
     * Update the "No items found" string to be displayed
     * @param none The string to use
     * @return SCChatMenu
     */
    public SCChatMenu none(String none) {
        this.none = none;
        return this;
    }

    public SCChatMenu showItems(BiFunction<Integer, Integer, List<Component>> func) {
        int start = (page - 1) * ITEMS_PER_PAGE;
        int maxPages = (int)Math.ceil((double) count / ITEMS_PER_PAGE);
        List<Component> lines = func.apply(start, ITEMS_PER_PAGE);

        if(lines.isEmpty()) {
            STEMCraftLib.error(sender, none);
            return this;
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
        return this;
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

}
package com.stemcraft.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class WorldDeleteEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final String worldName;

    public WorldDeleteEvent(String worldName) {
        this.worldName = worldName;
    }

    public String getWorldName() {
        return worldName;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
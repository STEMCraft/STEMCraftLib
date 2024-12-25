package com.stemcraft.listener;

import com.stemcraft.STEMCraftLib;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PlayerDropItemListener implements Listener {
    private final STEMCraftLib plugin;

    public PlayerDropItemListener(STEMCraftLib instance) {
        plugin = instance;
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        ItemStack item = event.getItemDrop().getItemStack();
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            if (plugin.itemGetAttrib(item, "destroy-on-drop", Integer.class, 0) == 1) {
                event.getItemDrop().remove();
            }

            if (plugin.itemGetAttrib(item, "no-drop", Integer.class, 0) == 1) {
                event.setCancelled(true);
            }
        }
    }
}



package com.github.kuramastone.regionstrial.selection;

import com.github.kuramastone.regionstrial.RegionAPI;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class SelectionListener implements Listener {

    private RegionAPI api;

    public SelectionListener(RegionAPI api) {
        this.api = api;
    }

    @EventHandler
    public void onBlockClick(PlayerInteractEvent event) {
        Action action = event.getAction();

        Player player = event.getPlayer();
        if (!player.hasPermission(RegionAPI.selectionPermission)) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }
        if (!api.isItemStackSelectionWand(event.getItem())) {
            return;
        }

        PlayerSelection selection = api.getOrCreateSelectionOf(event.getPlayer());

        Location l = block.getLocation();
        if (action == Action.LEFT_CLICK_BLOCK) {
            // dont repeat message if they are already the same
            if (!l.equals(selection.getCorner1())) {
                selection.setCorner1(block.getLocation());
                player.sendMessage("Set corner1 to [%s, %s, %s]".formatted(l.getBlockX(), l.getBlockY(), l.getBlockZ()));
            }
        }
        else if (action == Action.RIGHT_CLICK_BLOCK) {
            // dont repeat message if they are already the same
            if (!l.equals(selection.getCorner2())) {
                selection.setCorner2(block.getLocation());
                player.sendMessage("Set corner2 to [%s, %s, %s]".formatted(l.getBlockX(), l.getBlockY(), l.getBlockZ()));
            }
        }
        else {
            return;
        }

        event.setCancelled(true);


    }

}

package com.github.kuramastone.regionstrial.commands;

import com.github.kuramastone.regionstrial.RegionAPI;
import com.github.kuramastone.regionstrial.regions.Region;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

public class RegionWandCommand extends SubCommand {

    public RegionWandCommand(RegionAPI api) {
        super(api, 1, "wand");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission(RegionAPI.selectionPermission)) {
            sender.sendMessage(api.getMessage("commands.insufficient_permissions"));
            return true;
        }

        if (sender instanceof Player player) {
            player.getInventory().addItem(api.getWandItem());
            api.getOrCreateSelectionOf(player);
            player.sendMessage(api.getMessage("commands.regions.wand.success"));
        }
        else {
            sender.sendMessage("Only players may use a selection wand.");
        }

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {

        return List.of();
    }
}

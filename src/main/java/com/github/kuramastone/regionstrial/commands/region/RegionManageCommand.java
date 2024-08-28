package com.github.kuramastone.regionstrial.commands;

import com.github.kuramastone.regionstrial.RegionAPI;
import com.github.kuramastone.regionstrial.gui.RegionManagerGUI;
import com.github.kuramastone.regionstrial.gui.RegionSelectionGUI;
import com.github.kuramastone.regionstrial.regions.Region;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

public class RegionManageCommand extends SubCommand {

    public RegionManageCommand(RegionAPI api) {
        super(api, 1, "manage");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission(RegionAPI.createRegionPermission)) {
            sender.sendMessage(api.getMessage("commands.insufficient_permissions"));
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can manage regions via gui.");
            return true;
        }
        Player player = (Player) sender;

        if (args.length <= 1) {
            RegionSelectionGUI.openRegionMenu(player);
            return true;
        }
        else {
            String regionName = args[1];

            Region region = api.getRegion(regionName);
            if (region == null) {
                sender.sendMessage(api.getMessage("commands.regions.unknown_region").replace("{region}", regionName));
                return true;
            }

            RegionManagerGUI.openManagerMenu(player, region);
        }

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        int start = getArgumentLocation();

        // recommend regions
        if(args.length == start + 1) {
            return predictSuggestionStartingWith(args[start], api.getRegionNames());
        }


        return List.of();
    }
}

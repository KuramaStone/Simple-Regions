package com.github.kuramastone.regionstrial.commands.region;

import com.github.kuramastone.regionstrial.RegionAPI;
import com.github.kuramastone.regionstrial.commands.SubCommand;
import com.github.kuramastone.regionstrial.regions.Region;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

public class RegionCreateCommand extends SubCommand {

    public RegionCreateCommand(RegionAPI api) {
        super(api, 1, "create");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission(RegionAPI.createRegionPermission)) {
            sender.sendMessage(api.getMessage("commands.insufficient_permissions"));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("Usage: /region create <region> ");
            return true;
        }

        String regionName = args[1];
        Region region = api.getRegion(regionName);

        if (region != null) {
            sender.sendMessage(api.getMessage("commands.regions.region_already_exists").replace("{region}", regionName));
            return true;
        }
        region = new Region(regionName, new LinkedHashMap<>(), new HashSet<>(), new HashMap<>());

        api.addRegion(region);
        sender.sendMessage(api.getMessage("commands.regions.create.success").replace("{region}", region.getName()));
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        int start = getArgumentLocation();

        // recommend regions
        if(args.length == start + 1) {
            return predictSuggestionStartingWith(args[start], api.getRegionNames());
        }

        if(args.length == start + 2) {
            return List.of("<unique name>");
        }


        return List.of();
    }
}

package com.github.kuramastone.regionstrial.commands.region;

import com.github.kuramastone.regionstrial.RegionAPI;
import com.github.kuramastone.regionstrial.commands.SubCommand;
import com.github.kuramastone.regionstrial.regions.Region;
import org.bukkit.command.CommandSender;

import java.util.List;

public class RegionRenameCommand extends SubCommand {

    public RegionRenameCommand(RegionAPI api) {
        super(api, 1, "rename");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission(RegionAPI.createRegionPermission)) {
            sender.sendMessage(api.getMessage("commands.insufficient_permissions"));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("Usage: /region rename <region> <new name>");
            return true;
        }

        String regionName = args[1];
        String newName = args[2];

        Region region = api.getRegion(regionName);
        if (api.getRegion(newName) != null) {
            sender.sendMessage(api.getMessage("commands.regions.region_already_exists").replace("{region}", regionName));
            return true;
        }
        if (region == null) {
            sender.sendMessage(api.getMessage("commands.regions.unknown_region").replace("{region}", regionName));
            return true;
        }

        sender.sendMessage(api.getMessage("commands.regions.rename.success").replace("{region}", region.getName()));
        api.renameRegion(newName, region);

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        int start = getArgumentLocation();

        // recommend regions
        if (args.length == start + 1) {
            return predictSuggestionStartingWith(args[start], api.getRegionNames());
        }

        return List.of();
    }
}

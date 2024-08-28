package com.github.kuramastone.regionstrial.commands.region;

import com.github.kuramastone.regionstrial.RegionAPI;
import com.github.kuramastone.regionstrial.commands.SubCommand;
import com.github.kuramastone.regionstrial.regions.Region;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.UUID;

public class RegionWhitelistCommand extends SubCommand {

    public RegionWhitelistCommand(RegionAPI api) {
        super(api, 1, "whitelist");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission(RegionAPI.modifyWhitelistPermission)) {
            sender.sendMessage(api.getMessage("commands.insufficient_permissions"));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("Usage: /region whitelist <region> <player>");
            return true;
        }
        //
        else if (args.length == 2) {
            listWhitelist(sender, args);
        }
        else/* if(args.length >= 3)*/ {
            modifyWhitelist(sender, args);
        }

        return true;
    }

    private void modifyWhitelist(CommandSender sender, String[] args) {
        String regionName = args[1];
        String identifier = args[2];
        UUID uuid = getTargetByIdentifier(identifier);

        Region region = api.getRegion(regionName);

        if (region == null) {
            sender.sendMessage(api.getMessage("commands.regions.unknown_region").replace("{region}", regionName));
            return;
        }

        if (uuid == null) {
            sender.sendMessage(api.getMessage("commands.regions.unknown_identifier").replace("{player}", identifier));
            return;
        }

        if (region.isWhitelisted(uuid)) {
            region.removeWhitelistedEntity(uuid);
            sender.sendMessage(api.getMessage("commands.regions.whitelist.removed_player").replace("{player}", identifier));
        }
        else {
            sender.sendMessage(api.getMessage("commands.regions.whitelist.added_player").replace("{player}", identifier));
            region.addWhitelistedEntity(uuid);
        }
    }

    private void listWhitelist(CommandSender sender, String[] args) {
        String regionName = args[1];
        Region region = api.getRegion(regionName);

        if (region == null) {
            sender.sendMessage(api.getMessage("commands.regions.unknown_region").replace("{region}", regionName));
            return;
        }
        StringBuilder listText = new StringBuilder();

        for (UUID uuid : region.getWhitelistedEntities()) {
            String id = uuid.toString();

            // if uuid is not a currently alive entity, use player name
            if (Bukkit.getServer().getEntity(uuid) == null) {
                OfflinePlayer plyr = Bukkit.getOfflinePlayer(uuid);
                id = plyr.getName();
            }

            if (id == null) {
                id = uuid.toString();
            }
            listText.append(api.getMessage("commands.regions.whitelist.list_child").replace("{id}", id));
        }

        String fullMessage = api.getMessage("commands.regions.whitelist.list_parent").replace("{list}", listText);


        // split by lines in cae
        String[] lines = fullMessage.split("\n");
        sender.sendMessage(lines);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        int start = getArgumentLocation();

        // recommend regions
        if (args.length == start + 1) {
            return predictSuggestionStartingWith(args[start], api.getRegionNames());
        }
        if (args.length == start + 2) {
            return predictSuggestionStartingWith(args[start], getPlayerSuggestions(sender));
        }

        return List.of();
    }
}

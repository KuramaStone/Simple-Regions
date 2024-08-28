package com.github.kuramastone.regionstrial.commands;

import com.github.kuramastone.regionstrial.RegionAPI;
import com.github.kuramastone.regionstrial.gui.FlagGUI;
import com.github.kuramastone.regionstrial.regions.FlagScope;
import com.github.kuramastone.regionstrial.regions.Region;
import com.github.kuramastone.regionstrial.regions.RegionFlag;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class RegionFlagCommand extends SubCommand {

    public RegionFlagCommand(RegionAPI api) {
        super(api, 1, "flag");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission(RegionAPI.modifyFlagPermission)) {
            sender.sendMessage(api.getMessage("commands.insufficient_permissions"));
            return true;
        }

        // redirect to gui if only region is specified
        if (sender instanceof Player player && args.length == 2) {
            String regionName = args[1];
            Region region = api.getRegion(regionName);
            if (region == null) {
                sender.sendMessage(api.getMessage("commands.regions.unknown_region").replace("{region}", regionName));
                return true;
            }

            FlagGUI.openFlagMenu(player, region);
        }
        else {

            if (args.length < 4) {
                sender.sendMessage("Usage: /region flag <name> <flag> <state>");
                return true;
            }
            String regionName = args[1];
            String flagName = args[2];
            String scopeName = args[3];

            Region region = api.getRegion(regionName);
            RegionFlag flag = api.getFlag(flagName);
            FlagScope scope = FlagScope.getOrNull(scopeName);

            if (region == null) {
                sender.sendMessage(api.getMessage("commands.regions.unknown_region").replace("{region}", regionName));
                return true;
            }
            if (flag == null) {
                sender.sendMessage(api.getMessage("commands.regions.unknown_flag").replace("{flag}", flagName));
                return true;
            }
            if (scope == null) {
                sender.sendMessage(api.getMessage("commands.regions.unknown_flagscope").replace("{scope}", scopeName));
                return true;
            }

            region.setFlag(flag, scope);
            sender.sendMessage(api.getMessage("commands.regions.flag.success").replace("{region}", region.getName()).replace("{flag}", flag.getName()).replace("{scope}", scope.toString().toLowerCase()));

        }

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

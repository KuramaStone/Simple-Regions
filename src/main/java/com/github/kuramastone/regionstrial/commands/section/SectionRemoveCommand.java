package com.github.kuramastone.regionstrial.commands.section;

import com.github.kuramastone.regionstrial.RegionAPI;
import com.github.kuramastone.regionstrial.RegionPlugin;
import com.github.kuramastone.regionstrial.commands.SubCommand;
import com.github.kuramastone.regionstrial.regions.Region;
import com.github.kuramastone.regionstrial.selection.PlayerSelection;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class SectionRemoveCommand extends SubCommand {

    public SectionRemoveCommand(RegionAPI api) {
        super(api, 2, "remove");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage("Usage: /region section remove <region> <section>");
            return true;
        }

        String regionName = args[2];
        String additionName = args[3];

        Region region = api.getRegion(regionName);
        if (region == null) {
            sender.sendMessage(api.getMessage("commands.regions.unknown_region").replace("{region}", regionName));
            return true;
        }

        if (!region.doesSectionExist(additionName)) {
            sender.sendMessage(api.getMessage("commands.regions.section_doesnt_exist")
                    .replace("{region}", regionName)
                    .replace("{section}", additionName));
            return true;
        }

        region.removeSection(additionName);
        sender.sendMessage(api.getMessage("commands.regions.section.remove_success")
                .replace("{region}", region.getName())
                .replace("{section}", additionName));

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
            Region region = api.getRegion(args[start]);
            if(region != null) {
                return predictSuggestionStartingWith(args[start +  1], region.getSectionNames());
            }
        }


        return List.of();
    }
}

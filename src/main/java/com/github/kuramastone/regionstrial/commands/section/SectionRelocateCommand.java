package com.github.kuramastone.regionstrial.commands.section;

import com.github.kuramastone.regionstrial.RegionAPI;
import com.github.kuramastone.regionstrial.RegionPlugin;
import com.github.kuramastone.regionstrial.commands.SubCommand;
import com.github.kuramastone.regionstrial.regions.Region;
import com.github.kuramastone.regionstrial.selection.PlayerSelection;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class SectionRelocateCommand extends SubCommand {

    public SectionRelocateCommand(RegionAPI api) {
        super(api, 2, "relocate");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage("Usage: /region section relocate <region> <section>");
            return true;
        }

        String regionName = args[2];
        String sectionName = args[3];

        Region region = api.getRegion(regionName);
        if (region == null) {
            sender.sendMessage(api.getMessage("commands.regions.unknown_region").replace("{region}", regionName));
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use the selection tool.");
            return true;
        }

        if (!region.doesSectionExist(sectionName)) {
            sender.sendMessage(api.getMessage("commands.regions.section_doesnt_exist")
                    .replace("{region}", regionName)
                    .replace("{section}", sectionName));
            return true;
        }

        PlayerSelection selection = api.getOrCreateSelectionOf(player);
        if (!selection.isReady()) {
            sender.sendMessage(api.getMessage("commands.regions.selection_not_ready")
                    .replace("{region}", regionName)
                    .replace("{section}", sectionName));
            return true;
        }

        region.relocate(sectionName, selection);
        player.sendMessage(api.getMessage("commands.regions.section.relocate_success")
                .replace("{region}", region.getName())
                .replace("{section}", sectionName));

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

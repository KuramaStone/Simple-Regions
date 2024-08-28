package com.github.kuramastone.regionstrial.commands.section;

import com.github.kuramastone.regionstrial.RegionAPI;
import com.github.kuramastone.regionstrial.RegionPlugin;
import com.github.kuramastone.regionstrial.commands.SubCommand;
import com.github.kuramastone.regionstrial.commands.region.*;
import com.github.kuramastone.regionstrial.gui.RegionSelectionGUI;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ParentSectionCommand extends SubCommand {

    public ParentSectionCommand(RegionAPI api) {
        super(api, 1, "section");

        registerSubCommand(new SectionAddCommand(api));
        registerSubCommand(new SectionRemoveCommand(api));
        registerSubCommand(new SectionViewCommand(api));
        registerSubCommand(new SectionRelocateCommand(api));
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission(RegionAPI.createRegionPermission)) {
            sender.sendMessage(api.getMessage("commands.insufficient_permissions"));
            return true;
        }

        if(args.length <= getArgumentLocation()) {
            sender.sendMessage("Usage: /region section <add/remove/view/relocate>");
            return true;
        }
        else {
            String subcmd = args[getArgumentLocation()];

            for(SubCommand sub : this.subCommands) {
                // check if subcommand fits provided details
                if(sub.getSubcommand().equalsIgnoreCase(subcmd)) {
                    return sub.execute(sender, args);
                }
            }
        }

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {

        if(args.length == getArgumentLocation() + 1) {
            List<String> list = new ArrayList<>();
            for(SubCommand cmd : this.subCommands) {
                list.add(cmd.getSubcommand());
            }

            return predictSuggestionStartingWith(args[getArgumentLocation()], list);
        }
        else {

            String sub = args[getArgumentLocation()];
            for(SubCommand cmd : this.subCommands) {
                if(sub.equalsIgnoreCase(cmd.getSubcommand())) {
                    return cmd.tabComplete(sender, args);
                }
            }
        }

        return List.of();
    }
}













package com.github.kuramastone.regionstrial.commands.region;

import com.github.kuramastone.regionstrial.RegionAPI;
import com.github.kuramastone.regionstrial.commands.SubCommand;
import com.github.kuramastone.regionstrial.commands.section.ParentSectionCommand;
import com.github.kuramastone.regionstrial.gui.RegionSelectionGUI;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ParentRegionCommand extends SubCommand {

    public ParentRegionCommand(RegionAPI api) {
        super(api, 0, null);

        // subcommands
        registerSubCommand(new RegionCreateCommand(api));
        registerSubCommand(new RegionFlagCommand(api));
        registerSubCommand(new RegionManageCommand(api));
        registerSubCommand(new RegionRenameCommand(api));
        registerSubCommand(new RegionWandCommand(api));
        registerSubCommand(new RegionWhitelistCommand(api));

        // register other subcommand category
        registerSubCommand(new ParentSectionCommand(api));
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {

        if(args.length <= getArgumentLocation()) {
            openRegionMenu(sender, args);
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

        return false;
    }

    private void openRegionMenu(CommandSender sender, String[] args) {

        if(sender instanceof Player plyr) {
            if(!plyr.hasPermission(RegionAPI.menuPermission)) {
                sender.sendMessage(api.getMessage("commands.insufficient_permissions"));
                return;
            }

            RegionSelectionGUI.openRegionMenu(plyr);
        }

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

            String sub = args[0];
            for(SubCommand cmd : this.subCommands) {
                if(sub.equalsIgnoreCase(cmd.getSubcommand())) {
                    return cmd.tabComplete(sender, args);
                }
            }
        }

        return List.of();
    }
}













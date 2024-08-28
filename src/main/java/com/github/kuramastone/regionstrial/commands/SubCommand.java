package com.github.kuramastone.regionstrial.commands;

import com.github.kuramastone.regionstrial.RegionAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public abstract class SubCommand {

    protected final RegionAPI api;
    private final int argumentLocation;
    private final String subcommand;

    public SubCommand(RegionAPI api, int argumentLocation, String subcommand) {
        this.api = api;
        this.argumentLocation = argumentLocation;
        this.subcommand = subcommand;
    }


    public abstract boolean execute(CommandSender sender, String[] args);

    public abstract List<String> tabComplete(CommandSender sender, String[] args);

    protected List<String> predictSuggestionStartingWith(String prefix, Collection<String> col) {
        List<String> list = new ArrayList<>(col);
        List<String> valid = new ArrayList<>();

        if (prefix != null && !prefix.isEmpty()) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).toLowerCase().startsWith(prefix.toLowerCase(

                ))) {
                    valid.add(list.get(i));
                }
            }
        }
        else {
            return list;
        }

        //sort alphabetically
        valid.sort(String::compareTo);

        return valid;
    }

    protected List<String> getPlayersAndTargetSuggestion(CommandSender sender) {
        List<String> list = getPlayerSuggestions(sender);

        if (sender instanceof Player plyr) {
            Entity target = plyr.getTargetEntity(5);
            if (target != null && plyr.canSee(target)) {
                list.add(target.getUniqueId().toString());
            }
        }

        return list;
    }

    protected List<String> getPlayerSuggestions(CommandSender sender) {
        List<String> list = new ArrayList<>();

        Player mainPlayer = (Player) sender;

        for (Player plyr : sender.getServer().getOnlinePlayers()) {
            if (mainPlayer.canSee(plyr))
                list.add(plyr.getName());
        }

        return list;
    }

    protected UUID getTargetByIdentifier(String identifier) {
        // might be a username
        Entity entity = Bukkit.getServer().getPlayer(identifier);
        if (entity != null) {
            return entity.getUniqueId();
        }

        try {
            UUID uuid = UUID.fromString(identifier);

            // must be an offline player or an entity
            return uuid;

        }
        catch (IllegalArgumentException e) {
            // not a username or uuid.
            return null;
        }

    }

    public int getArgumentLocation() {
        return argumentLocation;
    }

    public String getSubcommand() {
        return subcommand;
    }
}
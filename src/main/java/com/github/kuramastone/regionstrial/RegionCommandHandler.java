package com.github.kuramastone.regionstrial;

import com.github.kuramastone.regionstrial.commands.region.ParentRegionCommand;
import com.github.kuramastone.regionstrial.gui.FlagGUI;
import com.github.kuramastone.regionstrial.gui.RegionManagerGUI;
import com.github.kuramastone.regionstrial.regions.FlagScope;
import com.github.kuramastone.regionstrial.regions.Region;
import com.github.kuramastone.regionstrial.regions.RegionFlag;
import com.github.kuramastone.regionstrial.gui.RegionSelectionGUI;
import com.github.kuramastone.regionstrial.selection.PlayerSelection;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class RegionCommandHandler implements CommandExecutor, TabExecutor {

    private RegionAPI api;
    private ParentRegionCommand regionCommand; // parent command

    public RegionCommandHandler(RegionAPI api) {
        this.api = api;
        regionCommand = new ParentRegionCommand(api);
    }

    public void register(JavaPlugin plugin) {
        plugin.getCommand("region").setTabCompleter(this);
        plugin.getCommand("region").setExecutor(this);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return regionCommand.tabComplete(sender, args);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return regionCommand.execute(sender, args);
    }

}

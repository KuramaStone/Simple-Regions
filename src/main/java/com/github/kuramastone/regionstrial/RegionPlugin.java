package com.github.kuramastone.regionstrial;

import com.github.kuramastone.regionstrial.gui.FlagGUI;
import com.github.kuramastone.regionstrial.gui.RegionManagerGUI;
import com.github.kuramastone.regionstrial.gui.RegionSelectionGUI;
import com.github.kuramastone.regionstrial.gui.WhitelistGUI;
import com.github.kuramastone.regionstrial.selection.SelectionListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class RegionPlugin extends JavaPlugin {

    public static RegionPlugin instance;
    public static Logger logger;
    private RegionAPI api;

    @Override
    public void onEnable() {
        instance = this;
        logger = getLogger();
        api = new RegionAPI();

        // register commands and events
        new RegionCommandHandler(api).register(this);
        getServer().getPluginManager().registerEvents(new RegionListener(api), this);
        getServer().getPluginManager().registerEvents(new SelectionListener(api), this);

        // load saved regions
        api.loadRegions();

        // prepare guis
        RegionManagerGUI.init(api);
        RegionSelectionGUI.init(api);
        WhitelistGUI.init(api);
        FlagGUI.init(api);
    }

    public RegionAPI getApi() {
        return api;
    }

    @Override
    public void onDisable() {
        if (api == null) {
            return;
        }
        api.disableAllOutlines();
        api.saveRegions();
    }

}

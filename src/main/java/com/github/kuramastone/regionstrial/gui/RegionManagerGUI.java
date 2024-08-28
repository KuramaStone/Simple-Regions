package com.github.kuramastone.regionstrial.gui;

import com.github.kuramastone.regionstrial.RegionAPI;
import com.github.kuramastone.regionstrial.gui.items.*;
import com.github.kuramastone.regionstrial.regions.*;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.gui.structure.Markers;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.CommandItem;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.window.Window;

import java.util.ArrayList;
import java.util.List;

public class RegionManagerGUI {

    private static RegionAPI api;

    public static void init(RegionAPI api) {
        RegionManagerGUI.api = api;
    }

    public static void openManagerMenu(Player player, Region region) {

        List<Item> itemList = new ArrayList<>();

        for (RegionSection sec : region.getSections().values()) {
            if (sec instanceof CubicRegion cube) {
                itemList.add(new CubicSectionEntryItem(region, cube));
            }
        }

        Gui gui = PagedGui.items()
                .setStructure(
                        "F # # I # W # # R",
                        "# . . . . . . . #",
                        "B # # < # > # # #")
                .addIngredient('#', new SimpleItem(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setDisplayName(" ")))
                .addIngredient('.', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
                .addIngredient('I', new RegionInformationItem(region))
                .addIngredient('W', new WhitelistEntryItem(region))
                .addIngredient('R', new RegionRenameItem(region))
                .addIngredient('F', new FlagMenuItem(region))
                .addIngredient('<', new BackPageItem())
                .addIngredient('>', new ForwardPageItem())
                .addIngredient('B', new CommandItem(new ItemBuilder(Material.SPRUCE_HANGING_SIGN).setDisplayName("Back"), "/region"))
                .setContent(itemList)
                .build();

        Window window = Window.single()
                .setViewer(player)
                .setTitle("Region Manager")
                .setGui(gui)
                .build();

        window.open();
    }








}

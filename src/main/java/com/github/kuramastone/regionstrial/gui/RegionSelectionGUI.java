package com.github.kuramastone.regionstrial.gui;

import com.github.kuramastone.regionstrial.RegionAPI;
import com.github.kuramastone.regionstrial.gui.items.BackPageItem;
import com.github.kuramastone.regionstrial.gui.items.ForwardPageItem;
import com.github.kuramastone.regionstrial.gui.items.RegionEntryItem;
import com.github.kuramastone.regionstrial.regions.Region;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.gui.structure.Markers;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.CommandItem;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.window.Window;

import java.util.ArrayList;
import java.util.List;

public class RegionSelectionGUI {

    private static RegionAPI api;

    public static void init(RegionAPI api) {
        RegionSelectionGUI.api = api;
    }

    public static void openRegionMenu(Player player) {

        List<Item> itemList = new ArrayList<>();

        for (Region region : api.getRegions()) {
            itemList.add(new RegionEntryItem(region));
        }

        Gui gui = PagedGui.items()
                .setStructure(
                        "# # # # # # # # #",
                        "# . . . . . . . #",
                        "# . . . . . . . #",
                        "# # # < # > # # #")
                .addIngredient('#', new SimpleItem(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setDisplayName(" ")))
                .addIngredient('.', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
                .addIngredient('<', new BackPageItem())
                .addIngredient('>', new ForwardPageItem())
                .setContent(itemList)
                .build();

        Window window = Window.single()
                .setViewer(player)
                .setTitle("Region Selection")
                .setGui(gui)
                .build();

        window.open();
    }


}

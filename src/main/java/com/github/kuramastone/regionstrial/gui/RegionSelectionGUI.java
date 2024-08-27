package com.github.kuramastone.regionstrial.gui;

import com.github.kuramastone.regionstrial.RegionAPI;
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
import xyz.xenondevs.invui.item.impl.controlitem.PageItem;
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
            itemList.add(new RegionItem(region));
        }

        Gui gui = PagedGui.items()
                .setStructure(
                        "# # # # # # # # #",
                        "# . . . . . . . #",
                        "# . . . . . . . #",
                        "# # # < # > # # #")
                .addIngredient('#', new SimpleItem(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setDisplayName(" ")))
                .addIngredient('.', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
                .addIngredient('<', new BackItem())
                .addIngredient('>', new ForwardItem())
                .setContent(itemList)
                .build();

        Window window = Window.single()
                .setViewer(player)
                .setTitle("Region Selection")
                .setGui(gui)
                .build();

        window.open();
    }

    private static class RegionItem extends CommandItem {

        public RegionItem(Region region) {
            super(buildRegionItem(region),
                    "/region manage %s".formatted(region.getName()));
        }

        private static ItemProvider buildRegionItem(Region region) {
            ItemBuilder ib = new ItemBuilder(Material.EMERALD)
                    .setDisplayName(region.getName())
                    .addLoreLines("Sections: " + region.getSectionNames().size());

            return ib;
        }

    }

    private static class BackItem extends PageItem {

        public BackItem() {
            super(false);
        }

        @Override
        public ItemProvider getItemProvider(PagedGui<?> gui) {
            ItemBuilder builder = new ItemBuilder(Material.RED_STAINED_GLASS_PANE);
            builder.setDisplayName("Previous page")
                    .addLoreLines(gui.hasPreviousPage()
                            ? "Go to page " + gui.getCurrentPage() + "/" + gui.getPageAmount()
                            : "You can't go further back");

            return builder;
        }


    }

    private static class ForwardItem extends PageItem {

        public ForwardItem() {
            super(true);
        }

        @Override
        public ItemProvider getItemProvider(PagedGui<?> gui) {
            ItemBuilder builder = new ItemBuilder(Material.GREEN_STAINED_GLASS_PANE);
            builder.setDisplayName("Next page")
                    .addLoreLines(gui.hasNextPage()
                            ? "Go to page " + (gui.getCurrentPage() + 2) + "/" + gui.getPageAmount()
                            : "There are no more pages");

            return builder;
        }

    }

}

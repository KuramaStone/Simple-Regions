package com.github.kuramastone.regionstrial.gui;

import com.github.kuramastone.regionstrial.RegionAPI;
import com.github.kuramastone.regionstrial.gui.items.BackPageItem;
import com.github.kuramastone.regionstrial.gui.items.FlagEntryItem;
import com.github.kuramastone.regionstrial.gui.items.ForwardPageItem;
import com.github.kuramastone.regionstrial.regions.FlagScope;
import com.github.kuramastone.regionstrial.regions.Region;
import com.github.kuramastone.regionstrial.regions.RegionFlag;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
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

public class FlagGUI {

    private static RegionAPI api;

    public static void init(RegionAPI api) {
        FlagGUI.api = api;
    }

    /**
     * Opens a menu showing players and entities inside the region. Clicking can toggle their whitelist status
     *
     * @param player
     * @param region
     */
    public static void openFlagMenu(Player player, Region region) {

        List<Item> itemList = new ArrayList<>();

        for (RegionFlag flag : api.getRegisteredFlags()) {
            itemList.add(new FlagEntryItem(region, flag));
        }

        Gui gui = PagedGui.items()
                .setStructure(
                        "# # # # # # # # #",
                        ". . . . . . . . .",
                        "B # # < # > # # #")
                .addIngredient('#', new SimpleItem(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setDisplayName(" ")))
                .addIngredient('.', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
                .addIngredient('<', new BackPageItem())
                .addIngredient('>', new ForwardPageItem())
                .addIngredient('B', new CommandItem(new ItemBuilder(Material.SPRUCE_HANGING_SIGN).setDisplayName("Back"), "/region manage %s".formatted(region.getName())))
                .setContent(itemList)
                .build();

        Window window = Window.single()
                .setViewer(player)
                .setTitle("Toggle Flags")
                .setGui(gui)
                .build();

        window.open();
    }
}

package com.github.kuramastone.regionstrial.gui;

import com.github.kuramastone.regionstrial.RegionAPI;
import com.github.kuramastone.regionstrial.gui.items.*;
import com.github.kuramastone.regionstrial.regions.Region;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.gui.structure.Markers;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.CommandItem;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.window.Window;

import java.util.*;

public class WhitelistGUI {

    private static RegionAPI api;

    public static void init(RegionAPI api) {
        WhitelistGUI.api = api;
    }

    /**
     * Opens a menu showing players and entities inside the region. Clicking can toggle their whitelist status
     *
     * @param player
     * @param region
     */
    public static void openWhitelistMenu(Player player, Region region) {

        List<Item> itemList = new ArrayList<>();

        List<Entity> withinRegion = new ArrayList<>(api.getSignificantEntitiesInsideRegion(region));
        // sort players to top
        withinRegion.sort(Comparator.comparingInt(entity -> entity.getType() == EntityType.PLAYER ? 0 : 1));

        Set<UUID> alreadyAdded = new HashSet<>();
        for (Entity entity : withinRegion) {
            alreadyAdded.add(entity.getUniqueId());
            itemList.add(new RegionWhitelistEntryItem(new EntitySkullItemProvider(region, entity.getUniqueId()), region, entity.getUniqueId()));
        }
        itemList.add(new SimpleItem(new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setDisplayName(" ")));
        for (UUID whiteListed : region.getWhitelistedEntities()) {
            if (!alreadyAdded.contains(whiteListed)) {
                itemList.add(new RegionWhitelistEntryItem(new EntitySkullItemProvider(region, whiteListed), region, whiteListed));
            }
        }

        Gui gui = PagedGui.items()
                .setStructure(
                        "1 # # # # # # # 2",
                        "# # # # # # # # #",
                        ". . . . . . . . .",
                        ". . . . . . . . .",
                        ". . . . . . . . .",
                        "B # # < # > # # #")
                .addIngredient('#', new SimpleItem(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setDisplayName(" ")))
                .addIngredient('.', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
                .addIngredient('1', new ToggleEntireWhitelistItem(false, withinRegion, region, Material.REDSTONE_BLOCK, ChatColor.RED + "Remove All Present"))
                .addIngredient('2', new ToggleEntireWhitelistItem(true, withinRegion, region, Material.EMERALD_BLOCK, ChatColor.GREEN + "Whitelist All Present"))
                .addIngredient('<', new BackPageItem())
                .addIngredient('>', new ForwardPageItem())
                .addIngredient('B', new CommandItem(new ItemBuilder(Material.SPRUCE_HANGING_SIGN).setDisplayName("Back"), "/region manage %s".formatted(region.getName())))
                .setContent(itemList)
                .build();

        Window window = Window.single()
                .setViewer(player)
                .setTitle("Whitelist Manager")
                .setGui(gui)
                .build();

        window.open();
    }

}

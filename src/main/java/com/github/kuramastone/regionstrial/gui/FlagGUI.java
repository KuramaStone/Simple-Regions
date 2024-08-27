package com.github.kuramastone.regionstrial.gui;

import com.github.kuramastone.regionstrial.RegionAPI;
import com.github.kuramastone.regionstrial.regions.FlagScope;
import com.github.kuramastone.regionstrial.regions.Region;
import com.github.kuramastone.regionstrial.regions.RegionFlag;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
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
import xyz.xenondevs.invui.item.impl.controlitem.PageItem;
import xyz.xenondevs.invui.window.Window;

import java.util.ArrayList;
import java.util.Comparator;
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
            itemList.add(new FlagItem(region, flag));
        }

        Gui gui = PagedGui.items()
                .setStructure(
                        "# # # # # # # # #",
                        ". . . . . . . . .",
                        "B # # < # > # # #")
                .addIngredient('#', new SimpleItem(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setDisplayName(" ")))
                .addIngredient('.', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
                .addIngredient('<', new BackItem())
                .addIngredient('>', new ForwardItem())
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

    private static class FlagItem extends SimpleItem {

        private Region region;
        private RegionFlag flag;

        public FlagItem(Region region, RegionFlag flag) {
            super((ItemProvider) null);
            this.region = region;
            this.flag = flag;
        }

        @Override
        public ItemProvider getItemProvider() {
            ItemBuilder ib = new ItemBuilder(Material.STONE);

            FlagScope scope = region.getFlagScopeFor(flag);
            ib.setDisplayName(flag.getName());
            ib.addLoreLines("Currently set to: " + scope.toString());

            if(scope == FlagScope.NONE) {
                ib.setMaterial(Material.PAPER);
                ib.addLoreLines("This flag is disabled.");
            }
            else if(scope == FlagScope.EVERYONE) {
                ib.setMaterial(Material.OAK_DOOR);
                ib.addLoreLines("Everyone is affected by this.");
            }
            else if(scope == FlagScope.WHITELIST) {
                ib.setMaterial(Material.IRON_BARS);
                ib.addLoreLines("Everyone except whitelisted players are affected.");
            }

            return ib;
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
            FlagScope scope = region.getFlagScopeFor(flag);

            if(scope == FlagScope.NONE) {
                scope = FlagScope.EVERYONE;
            }
            else if(scope == FlagScope.EVERYONE) {
                scope = FlagScope.WHITELIST;
            }
            else if(scope == FlagScope.WHITELIST) {
                scope = FlagScope.NONE;
            }

            region.setFlag(flag, scope);

            notifyWindows();
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

package com.github.kuramastone.regionstrial.gui;

import com.github.kuramastone.regionstrial.RegionAPI;
import com.github.kuramastone.regionstrial.regions.*;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.util.Vector;
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
                itemList.add(new CubicSectionItem(region, cube));
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
                .addIngredient('W', new WhitelistItem(region))
                .addIngredient('R', new RenameItem(region))
                .addIngredient('F', new FlagMenuItem(region))
                .addIngredient('<', new BackItem())
                .addIngredient('>', new ForwardItem())
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

    private static class FlagMenuItem extends CommandItem {

        public FlagMenuItem(Region region) {
            super(new ItemBuilder(Material.BLACK_BANNER).setDisplayName("Configure Flags"), "/region flag %s".formatted(region.getName()));
        }
    }

    private static class CubicSectionItem extends SimpleItem {

        private Region region;
        private String sectionName;

        public CubicSectionItem(Region region, CubicRegion section) {
            super((ItemProvider) null);
            this.region = region;
            this.sectionName = section.getName();
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
            if (clickType == ClickType.MIDDLE) {
                String cmd = "/region section relocate %s %s".formatted(region.getName(), sectionName);
                player.chat(cmd);
            }
            else if(clickType == ClickType.RIGHT) {
                String cmd = "/region section view %s %s".formatted(region.getName(), sectionName);
                player.chat(cmd);
            }

            notifyWindows();
        }

        @Override
        public ItemProvider getItemProvider() {

            CubicRegion section = (CubicRegion) region.getSection(sectionName);

            ItemBuilder ib = new ItemBuilder(Material.PAPER);
            Vector l = section.getLowCorner();
            String lowCorner = ChatColor.GREEN + "[%s, %s, %s]".formatted(l.getBlockX(), l.getBlockY(), l.getBlockZ()) + ChatColor.RESET;
            l = section.getHighCorner();
            String highCorner = ChatColor.RED + "[%s, %s, %s]".formatted(l.getBlockX(), l.getBlockY(), l.getBlockZ()) + ChatColor.RESET;

            ib.setMaterial(Material.BLUE_STAINED_GLASS_PANE);
            ib.setDisplayName(section.getName());
            ib.addLoreLines(
                    "Middle click to relocate to current selection.",
                    "Right click to highlight outline.",
                    "Cubic Region:",
                    "    Range: %s <-> %s".formatted(lowCorner, highCorner));


            return ib;
        }
    }

    private static class RegionInformationItem extends SimpleItem {

        private Region region;

        public RegionInformationItem(Region region) {
            super((ItemProvider) null);
            this.region = region;
        }

        @Override
        public ItemProvider getItemProvider() {
            ItemBuilder ib = new ItemBuilder(Material.ENCHANTED_BOOK);
            ib.addItemFlags(ItemFlag.HIDE_ENCHANTS);

            List<String> lore = new ArrayList<>();
            lore.add("Name: " + region.getName());
            lore.add("Sections: " + region.getSections().size());
            lore.add("");

            // add flags
            for (RegionFlag flag : api.getRegisteredFlags()) {
                FlagScope scope = region.getFlagScopeFor(flag);
                lore.add("%s: %s".formatted(flag.getName(), scope.toString()));
            }

            ib.setDisplayName("Region Information");
            ib.addLoreLines(lore.toArray(new String[0]));

            return ib;
        }
    }

    private static class RenameItem extends SimpleItem {

        private Region region;

        public RenameItem(Region region) {
            super(new ItemBuilder(Material.WRITABLE_BOOK).setDisplayName("Rename Region"));
            this.region = region;
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
            player.closeInventory();

            // open conversation
            RenameConversation conv = new RenameConversation(region);
            conv.startConversation(player);
        }
    }

    private static class WhitelistItem extends SimpleItem {

        private Region region;

        public WhitelistItem(Region region) {
            super((ItemProvider) null);
            this.region = region;
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
            player.closeInventory();
            WhitelistGUI.openWhitelistMenu(player, region);
        }

        @Override
        public ItemProvider getItemProvider() {
            ItemBuilder ib = new ItemBuilder(Material.SNOWBALL);

            ib.setDisplayName("Whitelist");
            ib.addLoreLines("Click to reveal the whitelist gui");

            return ib;
        }
    }

    /**
     * Simply calls notifyWindows() when clicked in addition to the command
     */
    private static class CommandUpdateItem extends CommandItem {

        public CommandUpdateItem(@NotNull ItemProvider itemProvider, @NotNull String command) {
            super(itemProvider, command);
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
            super.handleClick(clickType, player, event);
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

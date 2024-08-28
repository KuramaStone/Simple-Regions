package com.github.kuramastone.regionstrial.gui.items;

import com.github.kuramastone.regionstrial.regions.CubicRegion;
import com.github.kuramastone.regionstrial.regions.Region;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.SimpleItem;

/**
 * Item that represents a cubic section
 */
public class CubicSectionEntryItem extends SimpleItem {

        private Region region;
        private String sectionName;

        public CubicSectionEntryItem(Region region, CubicRegion section) {
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
            else if (clickType == ClickType.RIGHT) {
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
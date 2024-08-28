package com.github.kuramastone.regionstrial.gui.items;

import com.github.kuramastone.regionstrial.gui.WhitelistGUI;
import com.github.kuramastone.regionstrial.regions.Region;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.SimpleItem;

import java.util.Collection;

/**
     * Used to create a clickable item that toggles whitelist
     */
    public class ToggleEntireWhitelistItem extends SimpleItem {

        private boolean addToWhitelist;
        private Collection<Entity> list;
        private Region region;

        public ToggleEntireWhitelistItem(boolean addToWhitelist, Collection<Entity> list, Region region, Material mat, String name) {
            super(new ItemBuilder(mat).setDisplayName(name).addLoreLines("Double left click to use."));
            this.addToWhitelist = addToWhitelist;
            this.list = list;
            this.region = region;
        }

        /**
         * On click, add/clear whitelist
         *
         * @param clickType
         * @param player
         * @param event
         */
        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {

            if (clickType == ClickType.DOUBLE_CLICK) {
                if (addToWhitelist) {
                    for (Entity entity : list) {
                        region.addWhitelistedEntity(entity.getUniqueId());
                    }
                }
                else {
                    region.clearWhitelist();
                }

                // toggle whole inventory
                player.closeInventory();
                WhitelistGUI.openWhitelistMenu(player, region);
            }


        }
    }
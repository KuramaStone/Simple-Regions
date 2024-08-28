package com.github.kuramastone.regionstrial.gui.items;

import com.github.kuramastone.regionstrial.gui.WhitelistGUI;
import com.github.kuramastone.regionstrial.regions.Region;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.SimpleItem;

public class WhitelistItem extends SimpleItem {

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
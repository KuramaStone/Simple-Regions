package com.github.kuramastone.regionstrial.gui.items;

import com.github.kuramastone.regionstrial.gui.RenameConversation;
import com.github.kuramastone.regionstrial.regions.Region;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.SimpleItem;

public class RenameItem extends SimpleItem {

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
package com.github.kuramastone.regionstrial.gui.items;

import com.github.kuramastone.regionstrial.regions.Region;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.impl.SimpleItem;

import java.util.UUID;

public class RegionWhitelistItem extends SimpleItem {

        private UUID uuid;
        private Region region;

        public RegionWhitelistItem(@NotNull ItemProvider itemProvider, Region region, UUID uuid) {
            super(itemProvider);
            this.uuid = uuid;
            this.region = region;
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {

            //toggle on left click, always off on right
            if (clickType == ClickType.LEFT) {
                if (region.isWhitelisted(uuid)) {
                    region.removeWhitelistedEntity(uuid);
                }
                else {
                    region.addWhitelistedEntity(uuid);
                }
            }
            else if (clickType == ClickType.RIGHT) {
                region.removeWhitelistedEntity(uuid);
            }

            notifyWindows();

        }
    }
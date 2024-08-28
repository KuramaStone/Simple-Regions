package com.github.kuramastone.regionstrial.gui.items;

import com.github.kuramastone.regionstrial.regions.Region;
import org.bukkit.Material;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.CommandItem;

public class RegionEntryItem extends CommandItem {

        public RegionEntryItem(Region region) {
            super(buildRegionItem(region),
                    "/region manage %s".formatted(region.getName()));
        }

        private static ItemProvider buildRegionItem(Region region) {
            ItemBuilder ib = new ItemBuilder(Material.DIAMOND)
                    .setDisplayName(region.getName())
                    .addLoreLines("Sections: " + region.getSectionNames().size());

            return ib;
        }

    }
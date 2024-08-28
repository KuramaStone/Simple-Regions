package com.github.kuramastone.regionstrial.gui.items;

import com.github.kuramastone.regionstrial.regions.Region;
import org.bukkit.Material;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.CommandItem;

public class FlagMenuItem extends CommandItem {

        public FlagMenuItem(Region region) {
            super(new ItemBuilder(Material.BLACK_BANNER).setDisplayName("Configure Flags"), "/region flag %s".formatted(region.getName()));
        }
    }
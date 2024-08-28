package com.github.kuramastone.regionstrial.gui.items;

import com.github.kuramastone.regionstrial.RegionAPI;
import com.github.kuramastone.regionstrial.RegionPlugin;
import com.github.kuramastone.regionstrial.regions.FlagScope;
import com.github.kuramastone.regionstrial.regions.Region;
import com.github.kuramastone.regionstrial.regions.RegionFlag;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.SimpleItem;

import java.util.ArrayList;
import java.util.List;

public class RegionInformationItem extends SimpleItem {

    private static RegionAPI api;
    private Region region;

    public RegionInformationItem(Region region) {
        super((ItemProvider) null);
        api = RegionPlugin.instance.getApi();
        this.region = region;
    }

    @Override
    public ItemProvider getItemProvider() {
        ItemBuilder ib = new ItemBuilder(Material.ENCHANTED_BOOK);
        ib.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        List<String> lore = new ArrayList<>();
        lore.add("Name: " + region.getName());
        lore.add("Sections: " + region.getSections().size());
        for (String secName : region.getSectionNames()) {
            lore.add("  - %s".formatted(secName));
        }
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
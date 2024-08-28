package com.github.kuramastone.regionstrial.gui.items;

import com.github.kuramastone.regionstrial.regions.FlagScope;
import com.github.kuramastone.regionstrial.regions.Region;
import com.github.kuramastone.regionstrial.regions.RegionFlag;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.SimpleItem;

public class FlagEntryItem extends SimpleItem {

        private Region region;
        private RegionFlag flag;

        public FlagEntryItem(Region region, RegionFlag flag) {
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

            if (scope == FlagScope.NONE) {
                ib.setMaterial(Material.PAPER);
                ib.addLoreLines("This flag is disabled.");
            }
            else if (scope == FlagScope.EVERYONE) {
                ib.setMaterial(Material.OAK_DOOR);
                ib.addLoreLines("Everyone is affected by this.");
            }
            else if (scope == FlagScope.WHITELIST) {
                ib.setMaterial(Material.IRON_BARS);
                ib.addLoreLines("Everyone except whitelisted players are affected.");
            }

            return ib;
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
            FlagScope scope = region.getFlagScopeFor(flag);

            if (scope == FlagScope.NONE) {
                scope = FlagScope.EVERYONE;
            }
            else if (scope == FlagScope.EVERYONE) {
                scope = FlagScope.WHITELIST;
            }
            else if (scope == FlagScope.WHITELIST) {
                scope = FlagScope.NONE;
            }

            region.setFlag(flag, scope);

            notifyWindows();
        }
    }
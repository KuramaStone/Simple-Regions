package com.github.kuramastone.regionstrial.gui.items;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.github.kuramastone.regionstrial.regions.Region;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.item.builder.AbstractItemBuilder;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EntityItemProvider extends AbstractItemBuilder<EntityItemProvider> {

        private Region region;
        private String name;
        private EntityType type;
        private OfflinePlayer player;
        private UUID uuid;
        private PlayerProfile playerProfile;

        public EntityItemProvider(Region region, UUID uuid) {
            super((Material) null);
            this.region = region;
            this.uuid = uuid;

            Entity entity = Bukkit.getEntity(uuid);
            if (entity != null && entity.getType() != EntityType.PLAYER) {
                player = null;
                name = entity.getName();
                type = entity.getType();
            }
            else {
                player = Bukkit.getOfflinePlayer(uuid);
                name = player.getName();
                type = EntityType.PLAYER;
                if(player.isOnline()) {
                    // getting it like this ensures that the profile is already loaded.
                    playerProfile = Bukkit.getPlayer(uuid).getPlayerProfile();
                }
            }
        }

        @Override
        public @NotNull ItemStack get(@Nullable String lang) {
            List<String> lore = new ArrayList<>();

            setMaterial(getMaterialFor(type));
            if (region.isWhitelisted(uuid)) {
                setDisplayName(ChatColor.GREEN + this.name);
                lore.add(ChatColor.GREEN + "Entity is whitelisted.");

                addEnchantment(Enchantment.BANE_OF_ARTHROPODS, 1, true);
                addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            else {
                setDisplayName(ChatColor.RED + this.name);
                lore.add(ChatColor.RED + "Entity is NOT whitelisted.");
            }

            if (player != null) {
                if (!player.isOnline()) {
                    lore.add(ChatColor.UNDERLINE + "This player/entity cannot be found in the area.");
                }
            }


            clearLore();
            for (String line : lore) {
                addLoreLines(line);
            }

            ItemStack item = super.get(lang);

            // if entity is a player, insert their skin
            if (playerProfile != null) {
                SkullMeta skull = (SkullMeta) item.getItemMeta();
                skull.setPlayerProfile(playerProfile);
                item.setItemMeta(skull);
            }

            return item;
        }

        private static @NotNull Material getMaterialFor(EntityType type) {
            return switch (type) {
                case PLAYER -> Material.PLAYER_HEAD;
                case ZOMBIE -> Material.ZOMBIE_HEAD;
                case CREEPER -> Material.CREEPER_HEAD;
                case SKELETON -> Material.SKELETON_SKULL;
                default -> Material.STONE;
            };
        }
    }
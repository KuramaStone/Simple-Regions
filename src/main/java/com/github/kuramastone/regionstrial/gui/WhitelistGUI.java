package com.github.kuramastone.regionstrial.gui;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.github.kuramastone.regionstrial.RegionAPI;
import com.github.kuramastone.regionstrial.regions.Region;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.xenondevs.inventoryaccess.component.ComponentWrapper;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.gui.structure.Markers;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.AbstractItemBuilder;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.CommandItem;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.item.impl.controlitem.PageItem;
import xyz.xenondevs.invui.window.Window;

import java.awt.event.ComponentAdapter;
import java.util.*;

public class WhitelistGUI {

    private static RegionAPI api;

    public static void init(RegionAPI api) {
        WhitelistGUI.api = api;
    }

    /**
     * Opens a menu showing players and entities inside the region. Clicking can toggle their whitelist status
     *
     * @param player
     * @param region
     */
    public static void openWhitelistMenu(Player player, Region region) {

        List<Item> itemList = new ArrayList<>();

        List<Entity> withinRegion = new ArrayList<>(api.getSignificantEntitiesInsideRegion(region));
        // sort players to top
        withinRegion.sort(Comparator.comparingInt(entity -> entity.getType() == EntityType.PLAYER ? 0 : 1));

        Set<UUID> alreadyAdded = new HashSet<>();
        for (Entity entity : withinRegion) {
            alreadyAdded.add(entity.getUniqueId());
            itemList.add(new EntityWhitelistItem(new EntityItemProvider(region, entity.getUniqueId()), region, entity.getUniqueId()));
        }
        itemList.add(new SimpleItem(new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setDisplayName(" ")));
        for (UUID whiteListed : region.getWhitelistedEntities()) {
            if(!alreadyAdded.contains(whiteListed)) {
                itemList.add(new EntityWhitelistItem(new EntityItemProvider(region, whiteListed), region, whiteListed));
            }
        }

        Gui gui = PagedGui.items()
                .setStructure(
                        "1 # # # # # # # 2",
                        "# # # # # # # # #",
                        ". . . . . . . . .",
                        ". . . . . . . . .",
                        ". . . . . . . . .",
                        "B # # < # > # # #")
                .addIngredient('#', new SimpleItem(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setDisplayName(" ")))
                .addIngredient('.', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
                .addIngredient('1', new ToggleAllWhitelistItem(false, withinRegion, region, Material.REDSTONE_BLOCK, ChatColor.RED + "Remove All Present"))
                .addIngredient('2', new ToggleAllWhitelistItem(true, withinRegion, region, Material.EMERALD_BLOCK, ChatColor.GREEN + "Whitelist All Present"))
                .addIngredient('<', new BackItem())
                .addIngredient('>', new ForwardItem())
                .addIngredient('B', new CommandItem(new ItemBuilder(Material.SPRUCE_HANGING_SIGN).setDisplayName("Back"), "/region manage %s".formatted(region.getName())))
                .setContent(itemList)
                .build();

        Window window = Window.single()
                .setViewer(player)
                .setTitle("Whitelist Manager")
                .setGui(gui)
                .build();

        window.open();
    }

    private static class EntityItemProvider extends AbstractItemBuilder<EntityItemProvider> {

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

    /**
     * Used to create a clickable item that toggles whitelist
     */
    private static class ToggleAllWhitelistItem extends SimpleItem {

        private boolean addToWhitelist;
        private Collection<Entity> list;
        private Region region;

        public ToggleAllWhitelistItem(boolean addToWhitelist, Collection<Entity> list, Region region, Material mat, String name) {
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
                openWhitelistMenu(player, region);
            }


        }
    }

    private static class EntityWhitelistItem extends SimpleItem {

        private UUID uuid;
        private Region region;

        public EntityWhitelistItem(@NotNull ItemProvider itemProvider, Region region, UUID uuid) {
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

    private static class BackItem extends PageItem {

        public BackItem() {
            super(false);
        }

        @Override
        public ItemProvider getItemProvider(PagedGui<?> gui) {
            ItemBuilder builder = new ItemBuilder(Material.RED_STAINED_GLASS_PANE);
            builder.setDisplayName("Previous page")
                    .addLoreLines(gui.hasPreviousPage()
                            ? "Go to page " + gui.getCurrentPage() + "/" + gui.getPageAmount()
                            : "You can't go further back");

            return builder;
        }


    }

    private static class ForwardItem extends PageItem {

        public ForwardItem() {
            super(true);
        }

        @Override
        public ItemProvider getItemProvider(PagedGui<?> gui) {
            ItemBuilder builder = new ItemBuilder(Material.GREEN_STAINED_GLASS_PANE);
            builder.setDisplayName("Next page")
                    .addLoreLines(gui.hasNextPage()
                            ? "Go to page " + (gui.getCurrentPage() + 2) + "/" + gui.getPageAmount()
                            : "There are no more pages");

            return builder;
        }

    }

}

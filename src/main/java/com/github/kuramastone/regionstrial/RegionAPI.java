package com.github.kuramastone.regionstrial;

import com.github.kuramastone.regionstrial.mysql.RegionsDatabase;
import com.github.kuramastone.regionstrial.mysql.SQLProfile;
import com.github.kuramastone.regionstrial.mysql.YamlReader;
import com.github.kuramastone.regionstrial.regions.FlagScope;
import com.github.kuramastone.regionstrial.regions.Region;
import com.github.kuramastone.regionstrial.regions.RegionFlag;
import com.github.kuramastone.regionstrial.regions.RegionSection;
import com.github.kuramastone.regionstrial.selection.PlayerSelection;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.Permission;

import java.io.File;
import java.util.*;

public class RegionAPI {

    public static final boolean DEBUG_MODE = true;

    public static final Permission menuPermission = new Permission("region.menu");
    public static final Permission modifyWhitelistPermission = new Permission("region.whitelist");
    public static final Permission createRegionPermission = new Permission("region.create");
    public static final Permission modifyFlagPermission = new Permission("region.flag");
    public static final Permission bypassPermission = new Permission("region.bypass");
    public static final Permission selectionPermission = new Permission("region.selection");

    private ItemStack wandItem;

    private final RegionsDatabase database;
    private Map<String, Region> regionsByName;
    private Map<String, String> messages;
    private Map<String, RegionFlag> registeredFlags;
    private Map<UUID, PlayerSelection> activeSelections;

    public RegionAPI() {
        this.database = loadDatabase();
        this.regionsByName = new HashMap<>();
        this.registeredFlags = new HashMap<>();
        this.messages = new HashMap<>();
        this.activeSelections = new HashMap<>();

        loadMessages();
        createWand();
        registerDefaultFlags();

        // load regions from mysql
        loadRegions();
    }

    /**
     * Get a set of regions that exist at that location
     * @param location
     * @return
     */
    public Set<Region> getRegionsAt(Location location) {
        Objects.requireNonNull(location);

        Set<Region> set = new HashSet<>();
        for (Region region : this.regionsByName.values()) {
            if (region.isInsideRegion(location)) {
                set.add(region);
            }
        }

        return set;
    }

    /**
     * Returns the saved config key for this message.
     *
     * @param key
     * @return
     */
    public String getMessage(String key) {
        Objects.requireNonNull(key);

        return this.messages.getOrDefault(key, "Message not found in config.yml for '%s'".formatted(key));
    }

    public Region getRegion(String regionName) {
        Objects.requireNonNull(regionName);
        return this.regionsByName.get(regionName);
    }

    public RegionFlag getFlag(String flagName) {
        return this.registeredFlags.get(flagName);
    }

    /*
    Rename a region
     */
    public void renameRegion(String newName, Region region) {
        this.regionsByName.remove(region.getName());
        region.setName(newName);
        this.regionsByName.put(newName, region);
    }

    public void addRegion(Region region) {
        this.regionsByName.put(region.getName(), region);
    }

    public boolean doesPlayerHaveSelection(Player player) {
        return this.activeSelections.containsKey(player.getUniqueId());
    }

    public PlayerSelection getOrCreateSelectionOf(Player player) {
        PlayerSelection selection = this.activeSelections.getOrDefault(player.getUniqueId(), new PlayerSelection());
        this.activeSelections.put(player.getUniqueId(), selection);

        return selection;
    }

    public void disableAllOutlines() {
        for (Region region : this.regionsByName.values()) {
            region.stopAllOutlines();
        }
    }

    /**
     * Test if an itemstack is an example of a Selection Wand
     * @param item
     * @return
     */
    public boolean isItemStackSelectionWand(ItemStack item) {
        if (item == null) {
            return false;
        }
        return item.isSimilar(this.wandItem);
    }

    public ItemStack getWandItem() {
        return wandItem.clone();
    }

    /**
     * Register this flag with all regions. Any region will be able to toggle it.
     * @param flag
     */
    public void registerRegionFlag(RegionFlag flag) {
        Objects.requireNonNull(flag);

        if (this.registeredFlags.containsKey(flag.getName())) {
            throw new IllegalArgumentException("A flag of that name is already registered");
        }

        this.registeredFlags.put(flag.getName(), flag);
    }

    public Collection<Region> getRegions() {
        return this.regionsByName.values();
    }

    /**
     * Get named entities and players. Wide and narrow phases of search. First gets all nearby, then checks if they are within.
     *
     * @param region
     * @return
     */
    public List<Entity> getSignificantEntitiesInsideRegion(Region region) {
        List<Entity> list = new ArrayList<>();


        for (RegionSection sec : region.getSections().values()) {
            World world = Bukkit.getWorld(sec.getWorldUID());
            if (world != null) {
                Location center = sec.getCenter().toLocation(world);

                double r = sec.getMaxRangeFromCenter();
                list.addAll(center.getNearbyEntities(r, r, r));
            }

        }


        // all players have a custom name. i only want to keep named entities. this works perfect
        list.removeIf((entity) -> {
            // if the entity isnt a player, it is removed if it has no custom name
            if (entity.getType() != EntityType.PLAYER) {
                if (entity.customName() == null) {
                    return true;
                }
            }

            if (!region.isInsideRegion(entity.getLocation())) {
                return true;
            }

            return false;
        });

        return list;
    }

    public void saveRegions() {
        database.saveRegions(getRegions());
    }

    private RegionsDatabase loadDatabase() {
        /*
        This happening on the main thread will slow server starting until it either connects or doesnt. this isnt great,
        but it stops players from destroying regions before they are created. If it was necessary, we could likely do this via a thread and start it
        during an earlier startup phase so that it would be ready.
         */

        SQLProfile sql = loadSQLProfile();
        return new RegionsDatabase(sql.getUrl(), sql.getDatabaseName(), sql.getUser(), sql.getPassword(), sql.shouldUseH2());
    }

    public Collection<String> getRegionNames() {
        return this.regionsByName.keySet();
    }

    public Collection<String> getRegionFlagNames() {
        return this.registeredFlags.keySet();
    }

    public Collection<RegionFlag> getRegisteredFlags() {
        return this.registeredFlags.values();
    }

    public Collection<String> getFlagScopeNames() {
        Set<String> list = new HashSet<>();
        for (FlagScope scope : FlagScope.values()) {
            list.add(scope.toString().toLowerCase());
        }

        return list;
    }

    void loadRegions() {

        for (Region region : database.loadRegions()) {
            addRegion(region);
        }
    }

    private SQLProfile loadSQLProfile() {
        YamlReader config = new YamlReader(RegionPlugin.instance, "config.yml");
        config.installNewKeysFromDefault("admin");

        boolean useH2 = config.getBoolean("admin.sql.useH2");
        String databaseName = config.getString("admin.sql.database");

        SQLProfile profile;
        if (useH2) {
            File dataFolder = RegionPlugin.instance.getDataFolder();
            String dbPath = new File(dataFolder, databaseName).getAbsolutePath();
            String url = "jdbc:h2:" + dbPath;

            profile = new SQLProfile(
                    url,
                    databaseName,
                    "sa",
                    "",
                    true
            );
        }
        else {
            String url = config.getString("admin.sql.url");
            String username = config.getString("admin.sql.username");
            String password = config.getString("admin.sql.password");

            if(!url.startsWith("jdbc:mysql://")) {
                url = url + "jdbc:mysql://";
            }

            profile = new SQLProfile(
                    url,
                    databaseName,
                    username,
                    password,
                    false
            );
        }

        return profile;
    }

    private void registerDefaultFlags() {
        registerRegionFlag(RegionFlag.CAN_PLACE_BLOCK);
        registerRegionFlag(RegionFlag.CAN_BREAK_BLOCK);
        registerRegionFlag(RegionFlag.CAN_RECEIVE_DAMAGE);
        registerRegionFlag(RegionFlag.CAN_INTERACT);
    }

    private void createWand() {
        wandItem = new ItemStack(Material.STICK);
        ItemMeta meta = wandItem.getItemMeta();
        meta.displayName(Component.text(ChatColor.AQUA + "Select Corners."));
        meta.lore(List.of(Component.text("Just like World Edit")));
        meta.addEnchant(Enchantment.AQUA_AFFINITY, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        wandItem.setItemMeta(meta);
    }

    /**
     * Loads all keys under "messages" and stores them for easy retrieval elsewhere
     */
    private void loadMessages() {
        YamlReader config = new YamlReader(RegionPlugin.instance, "config.yml");
        for (String subkey : config.getKeys("messages", true)) {
            String key = "messages." + subkey;
            if (config.isSection(key)) {
                continue;
            }

            //RegionPlugin.logger.info("%s: %s".formatted(subkey, config.getString(key)));

            this.messages.put(subkey, ChatColor.translateAlternateColorCodes('&', config.getString(key)));
        }
    }
}























package com.github.kuramastone.regionstrial;

import com.github.kuramastone.regionstrial.regions.FlagScope;
import com.github.kuramastone.regionstrial.regions.Region;
import com.github.kuramastone.regionstrial.regions.RegionFlag;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Set;
import java.util.UUID;

/**
 * Since RegionFlags can be created by other developers, I will handle all flags outside the region class like them.
 * The similar events could have the whitelist and location check automated,
 */
public class RegionListener implements Listener {

    private RegionAPI api;

    public RegionListener(RegionAPI api) {
        this.api = api;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission(RegionAPI.bypassPermission)) {
            //return;
        }

        Set<Region> regions = api.getRegionsAt(player.getLocation());
        if (regions.isEmpty()) {
            return;
        }

        UUID uuid = player.getUniqueId();
        boolean isPermitted = isPermittedByRegions(RegionFlag.CAN_INTERACT, regions, uuid);

        if (!isPermitted) {
            event.setCancelled(true);
            player.sendMessage(api.getMessage("flags.regions.cannot_interact_here"));
            return;
        }

        // allow event to pass
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamageEntity(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        Set<Region> regions = api.getRegionsAt(entity.getLocation());
        if (regions.isEmpty()) {
            return;
        }

        // this lets them take damage i suppose
        if (event.getEntity().hasPermission(RegionAPI.bypassPermission)) {
            //return;
        }

        UUID uuid = entity.getUniqueId();
        boolean isPermitted = isPermittedByRegions(RegionFlag.CAN_RECEIVE_DAMAGE, regions, uuid);

        if (!isPermitted) {
            event.setCancelled(true);
            return;
        }

        // allow event to pass
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission(RegionAPI.bypassPermission)) {
            //return;
        }

        Set<Region> regions = api.getRegionsAt(player.getLocation());
        if (regions.isEmpty()) {
            return;
        }

        UUID uuid = player.getUniqueId();
        boolean isPermitted = isPermittedByRegions(RegionFlag.CAN_BREAK_BLOCK, regions, uuid);

        if (!isPermitted) {
            event.setCancelled(true);
            player.sendMessage(api.getMessage("flags.regions.cannot_mine_here"));
            return;
        }

        // allow event to pass
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission(RegionAPI.bypassPermission)) {
            //return;
        }

        Set<Region> regions = api.getRegionsAt(player.getLocation());
        if (regions.isEmpty()) {
            return;
        }

        UUID uuid = player.getUniqueId();
        boolean isPermitted = isPermittedByRegions(RegionFlag.CAN_PLACE_BLOCK, regions, uuid);

        if (!isPermitted) {
            event.setCancelled(true);
            player.sendMessage(api.getMessage("flags.regions.cannot_build_here"));
            return;
        }

        // allow event to pass
    }

    private boolean isPermittedByRegions(RegionFlag flag, Set<Region> regions, UUID uuid) {
        boolean isPermitted = true;
        for (Region region : regions) {
            FlagScope scope = region.getFlagScopeFor(flag);
            if (scope == FlagScope.NONE)
                isPermitted &= false;
            else if (scope == FlagScope.EVERYONE)
                isPermitted &= true;
            else if (scope == FlagScope.WHITELIST)
                isPermitted &= (region.isWhitelisted(uuid));
        }

        return isPermitted;
    }


}

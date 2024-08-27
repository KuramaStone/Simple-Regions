package com.github.kuramastone.regionstrial.selection;

import com.github.kuramastone.regionstrial.regions.CubicRegion;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.Objects;

public class PlayerSelection {

    private Location corner1;
    private Location corner2;

    public PlayerSelection() {
    }

    public CubicRegion selectionToRegion(String name) {
        Objects.requireNonNull(corner1);
        Objects.requireNonNull(corner2);

        Vector low = new Vector(
                Math.min(corner1.getX(), corner2.getX()),
                Math.min(corner1.getY(), corner2.getY()),
                Math.min(corner1.getZ(), corner2.getZ())
        );
        Vector high = new Vector(
                Math.max(corner1.getX(), corner2.getX()),
                Math.max(corner1.getY(), corner2.getY()),
                Math.max(corner1.getZ(), corner2.getZ())
        );

        CubicRegion region = new CubicRegion(name, corner1.getWorld().getUID(), low, high);
        return region;
    }

    public Location getCorner1() {
        return corner1;
    }

    public Location getCorner2() {
        return corner2;
    }

    public void setCorner1(Location corner1) {
        this.corner1 = corner1;
    }

    public void setCorner2(Location corner2) {
        this.corner2 = corner2;
    }

    /**
     * Returns true if both corners have been set
     * @return
     */
    public boolean isReady() {
        return corner1 != null && corner2 != null;
    }
}

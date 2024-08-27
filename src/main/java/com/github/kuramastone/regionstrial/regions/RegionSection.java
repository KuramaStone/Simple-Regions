package com.github.kuramastone.regionstrial.regions;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.UUID;

/*
Used in case we want to implement more complex regions later
 */
public interface RegionSection {

    public boolean isBlockInsideRegion(Location location);
    public String getName();

    /**
     * UID of world
     * @return
     */
    public UUID getWorldUID();

    /**
     * Center of the region
     * @return
     */
    public Vector getCenter();

    /**
     * From the furthest corner to the center
     * @return
     */
    public double getMaxRangeFromCenter();
}

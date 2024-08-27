package com.github.kuramastone.regionstrial.regions;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.Objects;
import java.util.UUID;

public class CubicRegion implements RegionSection {

    private String name;
    private UUID world_uuid; // uuid of world used
    private Vector lowCorner; // lowest value corner
    private Vector highCorner; // highest value corner
    private Vector center;
    private double maxRange;

    public CubicRegion(String name, UUID world_uuid, Vector lowCorner, Vector highCorner) {
        this.name = name;
        this.world_uuid = world_uuid;
        this.lowCorner = lowCorner;
        this.highCorner = highCorner;

        updateCorners();
    }

    public void updateCorners() {
        double lowX = Math.min(lowCorner.getX(), highCorner.getX());
        double lowY = Math.min(lowCorner.getY(), highCorner.getY());
        double lowZ = Math.min(lowCorner.getZ(), highCorner.getZ());

        double highX = Math.max(lowCorner.getX(), highCorner.getX());
        double highY = Math.max(lowCorner.getY(), highCorner.getY());
        double highZ = Math.max(lowCorner.getZ(), highCorner.getZ());

        this.lowCorner = new Vector(lowX, lowY, lowZ);
        this.highCorner = new Vector(highX, highY, highZ);
        center = lowCorner.getMidpoint(highCorner);
        maxRange = lowCorner.distance(center);
    }

    /**
     * INCLUSIVE-INCLUSIVE CHECK
     * See if Location is inside the given bounds
     *
     * @param
     * @return
     */
    @Override
    public boolean isBlockInsideRegion(Location l) {
        Objects.requireNonNull(l, "Provided Location is null");

        if (!world_uuid.equals(l.getWorld().getUID())) {
            return false;
        }

        // add 1 to properly check inclusively. The surface of the roof is not extended.
        return l.getX() >= lowCorner.getX() && l.getX() <= highCorner.getX() + 1 && l.getY() >= lowCorner.getY() && l.getY() <= highCorner.getY() && l.getZ() >= lowCorner.getZ() && l.getZ() <= highCorner.getZ() + 1;
    }

    public Vector getLowCorner() {
        return lowCorner;
    }

    public Vector getHighCorner() {
        return highCorner;
    }

    public UUID getWorldUUID() {
        return world_uuid;
    }

    public void setLowCorner(Vector loc) {
        this.lowCorner = loc;
        updateCorners();
    }

    public void setLowCorner(Location loc) {
        this.lowCorner = new Vector(loc.getX(), loc.getY(), loc.z());
        world_uuid = loc.getWorld().getUID();
        updateCorners();
    }


    public void setHighCorner(Vector loc) {
        this.highCorner = loc;
        updateCorners();
    }

    public void setHighCorner(Location loc) {
        this.highCorner = new Vector(loc.getX(), loc.getY(), loc.z());
        world_uuid = loc.getWorld().getUID();
        updateCorners();
    }

    public void setWorldUUID(UUID world_uuid) {
        this.world_uuid = world_uuid;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public UUID getWorldUID() {
        return world_uuid;
    }

    @Override
    public Vector getCenter() {
        return center;
    }

    @Override
    public double getMaxRangeFromCenter() {
        return maxRange;
    }

}

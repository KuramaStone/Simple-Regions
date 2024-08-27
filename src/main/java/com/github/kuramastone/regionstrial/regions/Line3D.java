package com.github.kuramastone.regionstrial.regions;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public class Line3D {
    private final double startX;
    private final double startY;
    private final double startZ;
    private final double endX;
    private final double endY;
    private final double endZ;

    public Line3D(double startX, double startY, double startZ, double endX, double endY, double endZ) {
        this.startX = startX;
        this.startY = startY;
        this.startZ = startZ;
        this.endX = endX;
        this.endY = endY;
        this.endZ = endZ;
    }

    public double getStartX() {
        return startX;
    }

    public double getStartY() {
        return startY;
    }

    public double getStartZ() {
        return startZ;
    }

    public double getEndX() { return endX; }

    public double getEndY() {
        return endY;
    }

    public double getEndZ() {
        return endZ;
    }

    public double getLength() {
        double deltaX = endX - startX;
        double deltaY = endY - startY;
        double deltaZ = endZ - startZ;
        return Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
    }

    public Vector getDirection() {
        double dx = endX - startX;
        double dy = endY - startY;
        double dz = endZ - startZ;
        double length = getLength();

        // if length under episilon, dont use to avoid exploding values. Minecraft functions on larger scales.
        if(Math.abs(length) < 0.001) {
            length = 1.0;
        }

        return new Vector(dx / length, dy / length, dz / length);
    }
}

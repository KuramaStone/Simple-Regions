package com.github.kuramastone.regionstrial.regions;

import com.github.kuramastone.regionstrial.RegionPlugin;
import org.bukkit.*;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/*

Create an outline of the cubic region. Subdivide it by animation cycles.
Each the ticks count to cycle through positions between a block. This should simulate an animation.

 */
public class CubicOutlineEffectRunnable implements Runnable {

    private static final int animation_cycle_steps = 2;

    // used for the animation cycle
    private World world;
    private int ticks;
    private List<Location> loadedPositions;
    private Color outlineColor;
    private BukkitTask task;
    private boolean active = false;

    public CubicOutlineEffectRunnable(Color color, CubicRegion region) {
        this.outlineColor = color;
        world = Bukkit.getWorld(region.getWorldUID());
        preloadOutline(region);
    }

    @Override
    public void run() {
        active = true;
        final float ox = 0.0f;
        final float oy = 0.0f;
        final float oz = 0.0f;
        final int count = 0; // use count 0 to allow extra data. does not mean 0 particles.

        /*


        TODO:
        Check that animation is pretty when it renders


         */

        int start = ticks % animation_cycle_steps;
        int size = this.loadedPositions.size();
        for (int i = start; i < size; i += animation_cycle_steps) {
            world.spawnParticle(Particle.DUST, loadedPositions.get(i), count, ox, oy, oz, new Particle.DustOptions(outlineColor, 1.0f));
        }
        //world.spawnParticle(Particle.DUST, loadedPositions.get(ticks % size), count, ox, oy, oz, new Particle.DustOptions(Color.BLUE, 1.0f));


        ticks++;
        // auto cancel after 15 minutes
        if (ticks >= (20 * 60 * 15)) {
            cancel();
        }
    }

    public synchronized void cancel() throws IllegalStateException {
        task.cancel();
        active = false;
    }

    /**
     * Iterate over all existing cubic regions and get the position.
     * Get the edge for each side of the shape. Subdivide per block to create an animation effect.
     * Very flexible, intended to be used later for other shapes as well
     */
    public void preloadOutline(CubicRegion region) {
        Objects.requireNonNull(world);


        List<Location> outlinePoints = new ArrayList<>();

        List<Line3D> edges = calculateEdges(region);

        // Subdivide each edge into smaller segments and generate Location points
        for (Line3D edge : edges) {
            double startX = edge.getStartX();
            double startY = edge.getStartY();
            double startZ = edge.getStartZ(); // Use startY for z-axis since Line2D is 2D

            double length = edge.getLength();

            double step_size = 1.0 / animation_cycle_steps;

            Vector dir = edge.getDirection().multiply(step_size);
            Location location = new Location(world, startX, startY, startZ);
            for (double l = 0; l < length; l += step_size) {
                Location point = location.add(dir).clone();
                outlinePoints.add(point);
            }

        }

        this.loadedPositions = outlinePoints;
    }

    // Define the 12 edges of the cuboid as Line2D objects
    private List<Line3D> calculateEdges(CubicRegion region) {
        region.updateCorners();
        Vector lowCorner = region.getLowCorner();
        Vector highCorner = region.getHighCorner();
        double xMin = lowCorner.getBlockX();
        double xMax = highCorner.getBlockX() + 1;
        double yMin = lowCorner.getBlockY();
        double yMax = highCorner.getBlockY() +  1;
        double zMin = lowCorner.getBlockZ();
        double zMax = highCorner.getBlockZ() + 1;

        List<Line3D> edges = new ArrayList<>();
        // Bottom face edges
        edges.add(new Line3D(xMin, yMin, zMin, xMax, yMin, zMin));
        edges.add(new Line3D(xMax, yMin, zMin, xMax, yMin, zMax));
        edges.add(new Line3D(xMax, yMin, zMax, xMin, yMin, zMax));
        edges.add(new Line3D(xMin, yMin, zMax, xMin, yMin, zMin));

        // Top face edges
        edges.add(new Line3D(xMin, yMax, zMin, xMax, yMax, zMin));
        edges.add(new Line3D(xMax, yMax, zMin, xMax, yMax, zMax));
        edges.add(new Line3D(xMax, yMax, zMax, xMin, yMax, zMax));
        edges.add(new Line3D(xMin, yMax, zMax, xMin, yMax, zMin));

        // Vertical edges
        edges.add(new Line3D(xMin, yMin, zMin, xMin, yMax, zMin));
        edges.add(new Line3D(xMax, yMin, zMin, xMax, yMax, zMin));
        edges.add(new Line3D(xMax, yMin, zMax, xMax, yMax, zMax));
        edges.add(new Line3D(xMin, yMin, zMax, xMin, yMax, zMax));

        return edges;
    }

    public void start(int outlineUpdateRate) {
        // ignore if already active
        if (isActive()) {
            return;
        }

        task = Bukkit.getServer().getScheduler().runTaskTimer(RegionPlugin.instance, this, 0, outlineUpdateRate);
    }

    public void stop() {
        if (!isActive())
            return;
        cancel();
    }

    public boolean isActive() {
        return this.active;
    }
}

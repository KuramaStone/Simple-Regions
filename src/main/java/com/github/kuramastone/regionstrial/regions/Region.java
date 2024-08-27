package com.github.kuramastone.regionstrial.regions;

import com.github.kuramastone.regionstrial.RegionPlugin;
import com.github.kuramastone.regionstrial.selection.PlayerSelection;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;

import java.util.*;

public class Region {

    private static final int OUTLINE_UPDATE_RATE = 7;

    private String name;
    private LinkedHashMap<String, RegionSection> sections; // bounds of sections
    private Set<UUID> whitelistedEntities; // what entities are affected by WHITELIST flags
    private Map<RegionFlag, FlagScope> flags; // flags and their assigned scope
    private LinkedHashMap<String, CubicOutlineEffectRunnable> outlinePerSection;

    public Region(String name, LinkedHashMap<String, RegionSection> sections, Set<UUID> whitelistedEntities, Map<RegionFlag, FlagScope> flags) {
        this.name = name;
        this.sections = sections;
        this.whitelistedEntities = whitelistedEntities;
        this.flags = flags;

        // load outlines at initialization
        if(Bukkit.getServer() != null) { // can be null during testing
            outlinePerSection = new LinkedHashMap<>(); // linked to remember order
            for (RegionSection section : sections.values()) {
                if (section instanceof CubicRegion cubic) {
                    outlinePerSection.put(cubic.getName(), new CubicOutlineEffectRunnable(RAINBOW[outlinePerSection.size() % RAINBOW.length], cubic));
                }
            }
        }
    }

    public boolean isOutlineActive(String sectionName) {
        CubicOutlineEffectRunnable effect = this.outlinePerSection.get(sectionName);

        if (effect == null) {
            return false;
        }

        return effect.isActive();
    }

    /**
     * Creates an outline effect instance if needed. updates it if already existing
     *
     * @param name
     */
    public void startUpdateOutlineEffect(String name) {
        Objects.requireNonNull(name);
        CubicOutlineEffectRunnable effect = this.outlinePerSection.get(name);

        if (effect != null) {
            effect.start(OUTLINE_UPDATE_RATE);
            return;
        }

        // create. after previous changes should not be possible, but as a backup we'll keep this.
        RegionSection section = sections.get(name);
        if (section instanceof CubicRegion cubic) {
            effect = new CubicOutlineEffectRunnable(RAINBOW[outlinePerSection.size() % RAINBOW.length], cubic);
            effect.start(OUTLINE_UPDATE_RATE); // begin bukkit task
            this.outlinePerSection.put(section.getName(), effect);
        }

    }

    /**
     * Stops outline effect on this section
     *
     * @param name
     */
    public void stopUpdateOutlineEffect(String name) {
        Objects.requireNonNull(name);
        CubicOutlineEffectRunnable effect = this.outlinePerSection.get(name);

        // cant cancel what doesnt exist
        if (effect == null) {
            return;
        }

        effect.stop();

    }

    public void stopAllOutlines() {
        for (CubicOutlineEffectRunnable run : this.outlinePerSection.values()) {
            run.stop();
        }
    }

    /**
     * Check all sections to see if any contain the location
     *
     * @param location
     * @return
     */
    public boolean isInsideRegion(Location location) {

        for (RegionSection section : this.sections.values()) {
            if (section.isBlockInsideRegion(location)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks to see if entity has been added to the region.
     *
     * @param uuid
     * @return
     */
    public boolean isWhitelisted(UUID uuid) {
        return this.whitelistedEntities.contains(uuid);
    }

    // getters/setters

    public void setFlag(RegionFlag flag, FlagScope scope) {
        Objects.requireNonNull(flag, "Flag must not be null");
        Objects.requireNonNull(scope, "Scope must not be null");
        this.flags.put(flag, scope);
    }

    /**
     * Gets the scope for this flag. if flag is not added yet, provide default
     *
     * @param flag
     * @return
     */
    public FlagScope getFlagScopeFor(RegionFlag flag) {
        return this.flags.getOrDefault(flag, FlagScope.EVERYONE);
    }

    /**
     * Note that this applies to any entity, not just players
     *
     * @param entityUUID
     */
    public void addWhitelistedEntity(UUID entityUUID) {
        Objects.requireNonNull(entityUUID, "Entity UUID must not be null");
        this.whitelistedEntities.add(entityUUID);
    }

    public void removeWhitelistedEntity(UUID entityUUID) {
        this.whitelistedEntities.remove(entityUUID);
    }

    public void updateOutline(String sectionName) {
        stopUpdateOutlineEffect(sectionName);
        startUpdateOutlineEffect(sectionName);
    }

    public void addSection(RegionSection section) {
        Objects.requireNonNull(section, "Section must not be null");
        this.sections.put(section.getName(), section);

        startUpdateOutlineEffect(section.getName());
    }

    public void removeSection(RegionSection section) {
        this.sections.remove(section.getName());
        stopUpdateOutlineEffect(section.getName());
        this.outlinePerSection.remove(section.getName());
    }

    public void removeSection(String additionName) {
        removeSection(this.sections.get(additionName));
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, RegionSection> getSections() {
        return sections;
    }

    public Set<UUID> getWhitelistedEntities() {
        return whitelistedEntities;
    }

    public Map<RegionFlag, FlagScope> getFlags() {
        return flags;
    }

    private static final Color[] RAINBOW = {
            Color.RED,
            Color.ORANGE,
            Color.YELLOW,
            Color.GREEN,
            Color.BLUE,
            Color.PURPLE,
            Color.BLACK,
            Color.WHITE
    };

    public boolean doesSectionExist(String additionName) {
        return this.sections.containsKey(additionName);
    }

    /*
    Update location by just updating selection area
     */
    public void relocate(String sectionName, PlayerSelection selection) {
        RegionSection section = this.sections.get(sectionName);
        RegionSection newSection = selection.selectionToRegion(sectionName);
        Objects.requireNonNull(section);
        Objects.requireNonNull(newSection);

        boolean wasActive = isOutlineActive(sectionName);
        if(wasActive) {
            stopUpdateOutlineEffect(sectionName);
        }
        this.sections.put(sectionName, newSection);
        if(wasActive) {
            if(newSection instanceof CubicRegion cube) {
                CubicOutlineEffectRunnable outline = this.outlinePerSection.get(sectionName);
                outline.preloadOutline(cube); // allow outline to be rebuilt for this
                startUpdateOutlineEffect(sectionName);
            }
        }
    }

    public Collection<String> getSectionNames() {
        return this.sections.keySet();
    }

    public void clearWhitelist() {
        this.whitelistedEntities.clear();
    }

    public RegionSection getSection(String sectionName) {
        return this.sections.get(sectionName);
    }
}

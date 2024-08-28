package com.github.kuramastone.regionstrial.tests;

import static org.junit.jupiter.api.Assertions.*;

import com.github.kuramastone.regionstrial.mysql.RegionSQL;
import com.github.kuramastone.regionstrial.mysql.RegionsDatabase;
import com.github.kuramastone.regionstrial.regions.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.*;
import java.util.*;

import org.bukkit.util.Vector;

public class TestRegionSQL {

    private static RegionsDatabase database;

    @BeforeAll
    public static void setUp() throws SQLException {
        boolean useH2 = true;

        String url;
        if (useH2)
            url = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1"; // In-memory database URL
        else
            url = "jdbc:mysql://localhost:3306/"; // local mysql server

        String databaseName = "testdb";
        String user = "regiondataplugin";
        String password = "securepassword123";

        database = new RegionsDatabase(url, databaseName, user, password, useH2);
    }

    @Test
    public void testAdd() {
        assertEquals(42, Integer.sum(19, 23));
    }

    @Test
    public void testSaveAndLoadRegion() throws SQLException {
        System.out.println("Running simple write/read test for regions");
        // Create a Region instance for testing
        Region region = createTestRegion("testregion", 5);

        // Save the region to the database
        RegionSQL.saveAllRegions(database, List.of(region));

        // Load the region from the database
        Map<String, Region> loadedRegions = RegionSQL.loadAllRegions(database);
        Region loadedRegion = loadedRegions.get(region.getName());

        compareRegions(region, loadedRegion);
    }

    private void compareRegions(Region region, Region loadedRegion) {
        // Validate the loaded region
        assertNotNull(loadedRegion);
        assertEquals(region.getName(), loadedRegion.getName());

        // Validate sections
        assertEquals(region.getSections().size(), loadedRegion.getSections().size());
        List<String> indexedKeys = new ArrayList<>(region.getSections().keySet());
        for (int i = 0; i < region.getSections().size(); i++) {
            RegionSection originalSection = region.getSections().get(indexedKeys.get(i));
            RegionSection loadedSection = loadedRegion.getSections().get(indexedKeys.get(i));
            assertEquals(originalSection.getName(), loadedSection.getName());
            assertEquals(originalSection.getClass(), loadedSection.getClass());

            if (originalSection instanceof CubicRegion) {
                CubicRegion cube1 = (CubicRegion) originalSection;
                CubicRegion cube2 = (CubicRegion) loadedSection;

                assertEquals(cube1.getLowCorner(), cube2.getLowCorner());
                assertEquals(cube1.getHighCorner(), cube2.getHighCorner());
                assertEquals(cube1.getWorldUID(), cube2.getWorldUID());
            }

        }

        // vlidate whitelisted entities
        assertEquals(region.getWhitelistedEntities(), loadedRegion.getWhitelistedEntities());

        // Validate flags
        assertEquals(region.getFlags(), loadedRegion.getFlags());
    }

    @Test
    public void complexSaveAndLoad() throws SQLException {
        System.out.println("Running complex write/read test for regions");
        int regionCount = 10;
        List<Region> regions = new ArrayList<>();
        for (int i = 0; i < regionCount; i++) {
            regions.add(createTestRegion("region0" + i, 4));
        }

        RegionSQL.saveAllRegions(database, regions);

        Map<String, Region> loaded = RegionSQL.loadAllRegions(database);

        for (int i = 0; i < regionCount; i++) {
            compareRegions(regions.get(i), loaded.get(regions.get(i).getName()));
        }

    }

    // Create a test region with some dummy data
    private Region createTestRegion(String name, int sectionCount) {
        String regionName = name;

        LinkedHashMap<String, RegionSection> sections = new LinkedHashMap<>();
        for (int i = 0; i < sectionCount; i++) {
            String n = "Section00" + i;
            sections.put(n, new CubicRegion(n, UUID.randomUUID(), new Vector(i * 10, 0, 0), new Vector(i * 10 + 10, 10, 10)));
        }

        Set<UUID> whitelistedEntities = new HashSet<>();
        whitelistedEntities.add(UUID.randomUUID());
        whitelistedEntities.add(UUID.randomUUID());

        Map<RegionFlag, FlagScope> flags = new HashMap<>();
        flags.put(new RegionFlag("FLAG1"), FlagScope.EVERYONE);
        flags.put(new RegionFlag("FLAG2"), FlagScope.NONE);
        flags.put(new RegionFlag("FLAG3"), FlagScope.WHITELIST);

        return new Region(regionName, sections, whitelistedEntities, flags);
    }

    private long getRegionIdByName(String name) throws SQLException {
        String query = "SELECT id FROM Regions WHERE name = ?";
        try (Connection conn = database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getLong("id");
            }
            else {
                throw new RuntimeException("Region with name " + name + " not found.");
            }
        }
    }
}

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

        //String url = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1"; // In-memory database URL
        String url = "jdbc:mysql://localhost:3306/"; // local mysql server
        String databaseName = "testdb";
        String user = "regiondataplugin";
        String password = "securepassword123";

        database = new RegionsDatabase(url, databaseName, user, password, false);
    }

    @Test
    public void testAdd() {
        assertEquals(42, Integer.sum(19, 23));
    }

    @Test
    public void testSaveAndLoadRegion() throws SQLException {
        // Create a Region instance for testing
        Region region = createTestRegion("testregion");

        // Save the region to the database
        RegionSQL.saveRegion(database, region);

        // Load the region from the database
        List<Region> loadedRegions = RegionSQL.loadAllRegions(database);
        Region loadedRegion = loadedRegions.get(0);

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
        }

        // vlidate whitelisted entities
        assertEquals(region.getWhitelistedEntities(), loadedRegion.getWhitelistedEntities());

        // Validate flags
        assertEquals(region.getFlags(), loadedRegion.getFlags());
    }

    // Create a test region with some dummy data
    private Region createTestRegion(String name) {
        String regionName = name;

        LinkedHashMap<String, RegionSection> sections = new LinkedHashMap<>();
        sections.put("Section001", new CubicRegion("Section001", UUID.randomUUID(), new Vector(0, 0, 0), new Vector(10, 10, 10)));

        Set<UUID> whitelistedEntities = new HashSet<>();
        whitelistedEntities.add(UUID.randomUUID());
        whitelistedEntities.add(UUID.randomUUID());

        Map<RegionFlag, FlagScope> flags = new HashMap<>();
        flags.put(new RegionFlag("FLAG1"), FlagScope.EVERYONE);
        flags.put(new RegionFlag("FLAG2"), FlagScope.NONE);
        flags.put(new RegionFlag("FLAG2"), FlagScope.WHITELIST);

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
            } else {
                throw new RuntimeException("Region with name " + name + " not found.");
            }
        }
    }
}

package com.github.kuramastone.regionstrial.mysql;

import com.github.kuramastone.regionstrial.regions.*;
import org.bukkit.util.Vector;

import java.sql.*;
import java.util.*;

public class RegionSQL {


    // Save all regions from a collection
    public static void saveAllRegions(RegionsDatabase database, Collection<Region> regions) throws SQLException {
        try (Connection connection = database.getConnection()) {
            connection.setAutoCommit(false);

            // Process each region
            for (Region region : regions) {
                // 1. Delete existing data
                deleteRegion(connection, region.getName());

                // 2. Insert region
                int regionId = insertRegion(connection, region.getName());

                // 3. Insert sections
                insertSections(connection, regionId, region.getSections());

                // 4. Insert whitelisted entities
                insertWhitelistedEntities(connection, regionId, region.getWhitelistedEntities());

                // 5. Insert flags
                insertFlags(connection, regionId, region.getFlags());
            }

            connection.commit();
        }
    }

    // Load All Regions Method
    public static Map<String, Region> loadAllRegions(RegionsDatabase database) throws SQLException {
        Map<String, Region> regions = new HashMap<>();

        try (Connection connection = database.getConnection()) {
            // Get all regions
            String getAllRegions = "SELECT id, name FROM regions";
            try (PreparedStatement stmt = connection.prepareStatement(getAllRegions);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    int regionId = rs.getInt("id");
                    String name = rs.getString("name");

                    // Load sections, entities, and flags for each region
                    LinkedHashMap<String, RegionSection> sections = loadSections(connection, regionId);
                    Set<UUID> entities = loadWhitelistedEntities(connection, regionId);
                    Map<RegionFlag, FlagScope> flags = loadFlags(connection, regionId);

                    Region region = new Region(name, sections, entities, flags);
                    regions.put(name, region);
                }
            }
        }

        return regions;
    }

    private static LinkedHashMap<String, RegionSection> loadSections(Connection connection, int regionId) throws SQLException {
        LinkedHashMap<String, RegionSection> sections = new LinkedHashMap<>();
        String getSections = "SELECT * FROM region_sections WHERE region_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(getSections)) {
            stmt.setInt(1, regionId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String sectionName = rs.getString("section_name");
                    UUID worldUuid = convertBytesToUUID(rs.getBytes("world_uuid"));
                    Vector lowCorner = new Vector(rs.getDouble("low_corner_x"), rs.getDouble("low_corner_y"), rs.getDouble("low_corner_z"));
                    Vector highCorner = new Vector(rs.getDouble("high_corner_x"), rs.getDouble("high_corner_y"), rs.getDouble("high_corner_z"));
                    sections.put(sectionName, new CubicRegion(sectionName, worldUuid, lowCorner, highCorner));
                }
            }
        }

        return sections;
    }

    private static Set<UUID> loadWhitelistedEntities(Connection connection, int regionId) throws SQLException {
        Set<UUID> entities = new HashSet<>();
        String getEntities = "SELECT entity_uuid FROM whitelisted_entities WHERE region_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(getEntities)) {
            stmt.setInt(1, regionId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    entities.add(convertBytesToUUID(rs.getBytes("entity_uuid")));
                }
            }
        }

        return entities;
    }

    private static Map<RegionFlag, FlagScope> loadFlags(Connection connection, int regionId) throws SQLException {
        Map<RegionFlag, FlagScope> flags = new HashMap<>();
        String getFlags = "SELECT flag_name, scope FROM flags WHERE region_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(getFlags)) {
            stmt.setInt(1, regionId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    RegionFlag flag = new RegionFlag(rs.getString("flag_name"));
                    FlagScope scope = FlagScope.valueOf(rs.getString("scope"));
                    flags.put(flag, scope);
                }
            }
        }

        return flags;
    }

    private static void deleteRegion(Connection connection, String regionName) throws SQLException {
        String deleteSections = "DELETE FROM region_sections WHERE region_id = (SELECT id FROM regions WHERE name = ?)";
        String deleteWhitelistedEntities = "DELETE FROM whitelisted_entities WHERE region_id = (SELECT id FROM regions WHERE name = ?)";
        String deleteFlags = "DELETE FROM flags WHERE region_id = (SELECT id FROM regions WHERE name = ?)";
        String deleteRegion = "DELETE FROM regions WHERE name = ?";

        try (PreparedStatement deleteSectionsStmt = connection.prepareStatement(deleteSections);
             PreparedStatement deleteWhitelistedEntitiesStmt = connection.prepareStatement(deleteWhitelistedEntities);
             PreparedStatement deleteFlagsStmt = connection.prepareStatement(deleteFlags);
             PreparedStatement deleteRegionStmt = connection.prepareStatement(deleteRegion)) {

            deleteSectionsStmt.setString(1, regionName);
            deleteSectionsStmt.executeUpdate();

            deleteWhitelistedEntitiesStmt.setString(1, regionName);
            deleteWhitelistedEntitiesStmt.executeUpdate();

            deleteFlagsStmt.setString(1, regionName);
            deleteFlagsStmt.executeUpdate();

            deleteRegionStmt.setString(1, regionName);
            deleteRegionStmt.executeUpdate();
        }
    }

    private static int insertRegion(Connection connection, String name) throws SQLException {
        String insertRegion = "INSERT INTO regions (name) VALUES (?)";
        try (PreparedStatement stmt = connection.prepareStatement(insertRegion, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, name);
            stmt.executeUpdate();

            try (var rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                else {
                    throw new SQLException("Failed to retrieve region ID.");
                }
            }
        }
    }

    private static void insertSections(Connection connection, int regionId, Map<String, RegionSection> sections) throws SQLException {
        String insertSection = "INSERT INTO region_sections (region_id, section_name, world_uuid, low_corner_x, low_corner_y, low_corner_z, high_corner_x, high_corner_y, high_corner_z) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(insertSection)) {
            for (RegionSection section : sections.values()) {
                if (section instanceof CubicRegion) {
                    CubicRegion cubic = (CubicRegion) section;
                    stmt.setInt(1, regionId);
                    stmt.setString(2, cubic.getName());
                    stmt.setBytes(3, convertUUIDToBytes(cubic.getWorldUID()));
                    stmt.setDouble(4, cubic.getLowCorner().getX());
                    stmt.setDouble(5, cubic.getLowCorner().getY());
                    stmt.setDouble(6, cubic.getLowCorner().getZ());
                    stmt.setDouble(7, cubic.getHighCorner().getX());
                    stmt.setDouble(8, cubic.getHighCorner().getY());
                    stmt.setDouble(9, cubic.getHighCorner().getZ());
                    stmt.addBatch();
                }
            }
            stmt.executeBatch();
        }
    }

    private static void insertWhitelistedEntities(Connection connection, int regionId, Set<UUID> entities) throws SQLException {
        String insertEntity = "INSERT INTO whitelisted_entities (region_id, entity_uuid) VALUES (?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(insertEntity)) {
            for (UUID entity : entities) {
                stmt.setInt(1, regionId);
                stmt.setBytes(2, convertUUIDToBytes(entity));
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    private static void insertFlags(Connection connection, int regionId, Map<RegionFlag, FlagScope> flags) throws SQLException {
        String insertFlag = "INSERT INTO flags (region_id, flag_name, scope) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(insertFlag)) {
            for (Map.Entry<RegionFlag, FlagScope> entry : flags.entrySet()) {
                stmt.setInt(1, regionId);
                stmt.setString(2, entry.getKey().getName());
                stmt.setString(3, entry.getValue().name());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    private static byte[] convertUUIDToBytes(UUID uuid) {
        byte[] bytes = new byte[16];
        for (int i = 0; i < 8; i++) {
            bytes[i] = (byte) (uuid.getMostSignificantBits() >> (8 * (7 - i)));
        }
        for (int i = 0; i < 8; i++) {
            bytes[8 + i] = (byte) (uuid.getLeastSignificantBits() >> (8 * (7 - i)));
        }
        return bytes;
    }

    private static UUID convertBytesToUUID(byte[] bytes) {
        long mostSigBits = 0;
        long leastSigBits = 0;
        for (int i = 0; i < 8; i++) {
            mostSigBits = (mostSigBits << 8) | (bytes[i] & 0xFF);
        }
        for (int i = 8; i < 16; i++) {
            leastSigBits = (leastSigBits << 8) | (bytes[i] & 0xFF);
        }
        return new UUID(mostSigBits, leastSigBits);
    }

    public static void createSchema(RegionsDatabase database) throws SQLException {

        String schema = """
                CREATE TABLE regions (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(255) UNIQUE NOT NULL
                );
                                
                CREATE TABLE region_sections (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    region_id INT,
                    section_name VARCHAR(255),
                    world_uuid BINARY(16),
                    low_corner_x DOUBLE,
                    low_corner_y DOUBLE,
                    low_corner_z DOUBLE,
                    high_corner_x DOUBLE,
                    high_corner_y DOUBLE,
                    high_corner_z DOUBLE,
                    FOREIGN KEY (region_id) REFERENCES regions(id)
                );
                                
                CREATE TABLE whitelisted_entities (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    region_id INT,
                    entity_uuid BINARY(16),
                    FOREIGN KEY (region_id) REFERENCES regions(id)
                );
                                
                CREATE TABLE flags (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    region_id INT,
                    flag_name VARCHAR(255),
                    scope VARCHAR(255),
                    FOREIGN KEY (region_id) REFERENCES regions(id)
                );
                                
                """;

        String[] statements = schema.split("(?<=;)\s*");

        try (Connection conn = database.getConnection();
             Statement stmt = conn.createStatement()) {

            for (String sql : statements) {
                if (!sql.trim().isEmpty()) {
                    stmt.execute(sql);
                }
            }

            System.out.println("Database schema created successfully.");
        }
        catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

}

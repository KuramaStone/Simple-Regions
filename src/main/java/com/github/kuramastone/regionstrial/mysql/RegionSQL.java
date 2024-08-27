package com.github.kuramastone.regionstrial.mysql;

import com.github.kuramastone.regionstrial.regions.*;
import org.bukkit.util.Vector;

import java.sql.*;
import java.util.*;

public class RegionSQL {


    public static List<Region> loadAllRegions(RegionsDatabase database) throws SQLException {
        String selectAllRegionsSQL = "SELECT id FROM Regions";

        List<Region> regions = new ArrayList<>();

        try (Connection conn = database.getConnection();
             PreparedStatement allRegionsStmt = conn.prepareStatement(selectAllRegionsSQL);
             PreparedStatement regionStmt = conn.prepareStatement("SELECT name FROM Regions WHERE id = ?");
             PreparedStatement sectionStmt = conn.prepareStatement("SELECT id, section_type, section_order, name FROM RegionSections WHERE region_id = ? ORDER BY section_order ASC");
             PreparedStatement cubicStmt = conn.prepareStatement("SELECT world_uuid, low_x, low_y, low_z, high_x, high_y, high_z FROM CubicRegions WHERE id = ?");
             PreparedStatement entityStmt = conn.prepareStatement("SELECT entity_uuid FROM WhitelistedEntities WHERE region_id = ?");
             PreparedStatement flagStmt = conn.prepareStatement("SELECT flag_name, flag_scope FROM RegionFlags WHERE region_id = ?")) {

            ResultSet allRegionsRS = allRegionsStmt.executeQuery();
            while (allRegionsRS.next()) {
                long regionId = allRegionsRS.getLong("id");

                // Load the region
                regionStmt.setLong(1, regionId);
                ResultSet regionRS = regionStmt.executeQuery();

                if (regionRS.next()) {
                    String regionName = regionRS.getString("name");

                    // Load sections
                    LinkedHashMap<String, RegionSection> sections = new LinkedHashMap<>();
                    sectionStmt.setLong(1, regionId);
                    ResultSet sectionRS = sectionStmt.executeQuery();
                    while (sectionRS.next()) {
                        long sectionId = sectionRS.getLong("id");
                        String sectionType = sectionRS.getString("section_type");
                        String sectionName = sectionRS.getString("name"); // Correctly use the section name

                        if ("CUBIC".equals(sectionType)) {
                            cubicStmt.setLong(1, sectionId);
                            ResultSet cubicRS = cubicStmt.executeQuery();
                            if (cubicRS.next()) {
                                UUID worldUUID = UUID.fromString(cubicRS.getString("world_uuid"));
                                Vector lowCorner = new Vector(cubicRS.getDouble("low_x"), cubicRS.getDouble("low_y"), cubicRS.getDouble("low_z"));
                                Vector highCorner = new Vector(cubicRS.getDouble("high_x"), cubicRS.getDouble("high_y"), cubicRS.getDouble("high_z"));
                                sections.put(sectionName, new CubicRegion(sectionName, worldUUID, lowCorner, highCorner)); // Use sectionName
                            }
                        }
                    }

                    // Load whitelisted entities
                    Set<UUID> whitelistedEntities = new HashSet<>();
                    entityStmt.setLong(1, regionId);
                    ResultSet entityRS = entityStmt.executeQuery();
                    while (entityRS.next()) {
                        whitelistedEntities.add(UUID.fromString(entityRS.getString("entity_uuid")));
                    }

                    // Load flags
                    Map<RegionFlag, FlagScope> flags = new HashMap<>();
                    flagStmt.setLong(1, regionId);
                    ResultSet flagRS = flagStmt.executeQuery();
                    while (flagRS.next()) {
                        flags.put(new RegionFlag(flagRS.getString("flag_name")), FlagScope.valueOf(flagRS.getString("flag_scope")));
                    }

                    // Create the Region object
                    Region region = new Region(regionName, sections, whitelistedEntities, flags);
                    regions.add(region);
                }
            }
        }
        return regions;
    }


    public static void saveRegion(RegionsDatabase database, Region region) throws SQLException {
        String insertRegionSQL = "INSERT INTO Regions (name) VALUES (?)";
        String insertSectionSQL = "INSERT INTO RegionSections (region_id, section_type, section_order, name) VALUES (?, ?, ?, ?)";
        String insertCubicRegionSQL = "INSERT INTO CubicRegions (id, world_uuid, low_x, low_y, low_z, high_x, high_y, high_z) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String insertWhitelistedEntitySQL = "INSERT INTO WhitelistedEntities (region_id, entity_uuid) VALUES (?, ?)";
        String insertFlagSQL = "INSERT INTO RegionFlags (region_id, flag_name, flag_scope) VALUES (?, ?, ?)";

        try (Connection conn = database.getConnection();
             PreparedStatement insertRegionStmt = conn.prepareStatement(insertRegionSQL, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement insertSectionStmt = conn.prepareStatement(insertSectionSQL);
             PreparedStatement insertCubicStmt = conn.prepareStatement(insertCubicRegionSQL);
             PreparedStatement insertEntityStmt = conn.prepareStatement(insertWhitelistedEntitySQL);
             PreparedStatement insertFlagStmt = conn.prepareStatement(insertFlagSQL)) {

            // Insert Region
            insertRegionStmt.setString(1, region.getName());
            insertRegionStmt.executeUpdate();
            ResultSet generatedKeys = insertRegionStmt.getGeneratedKeys();
            long regionId;
            if (generatedKeys.next()) {
                regionId = generatedKeys.getLong(1);
            }
            else {
                throw new SQLException("Creating region failed, no ID obtained.");
            }

            // Insert Sections
            List<String> indexedKeys = new ArrayList<>(region.getSections().keySet());
            for (int i = 0; i < region.getSections().size(); i++) {
                RegionSection section = region.getSections().get(indexedKeys.get(i));
                String sectionType = section instanceof CubicRegion ? "CUBIC" : "SPHERICAL";
                insertSectionStmt.setLong(1, regionId);
                insertSectionStmt.setString(2, sectionType);
                insertSectionStmt.setInt(3, i);
                insertSectionStmt.setString(4, section.getName());
                insertSectionStmt.addBatch();

                // Insert specific section details
                if (section instanceof CubicRegion) {
                    CubicRegion cubicRegion = (CubicRegion) section;
                    insertCubicStmt.setLong(1, regionId);
                    insertCubicStmt.setString(2, cubicRegion.getWorldUUID().toString());
                    insertCubicStmt.setDouble(3, cubicRegion.getLowCorner().getX());
                    insertCubicStmt.setDouble(4, cubicRegion.getLowCorner().getY());
                    insertCubicStmt.setDouble(5, cubicRegion.getLowCorner().getZ());
                    insertCubicStmt.setDouble(6, cubicRegion.getHighCorner().getX());
                    insertCubicStmt.setDouble(7, cubicRegion.getHighCorner().getY());
                    insertCubicStmt.setDouble(8, cubicRegion.getHighCorner().getZ());
                    insertCubicStmt.addBatch();
                }
            }
            insertSectionStmt.executeBatch();
            insertCubicStmt.executeBatch();

            // Insert Whitelisted Entities
            for (UUID entityId : region.getWhitelistedEntities()) {
                insertEntityStmt.setLong(1, regionId);
                insertEntityStmt.setString(2, entityId.toString());
                insertEntityStmt.addBatch();
            }
            insertEntityStmt.executeBatch();

            // Insert Flags
            for (Map.Entry<RegionFlag, FlagScope> entry : region.getFlags().entrySet()) {
                insertFlagStmt.setLong(1, regionId);
                insertFlagStmt.setString(2, entry.getKey().getName());
                insertFlagStmt.setString(3, entry.getValue().name());
                insertFlagStmt.addBatch();
            }
            insertFlagStmt.executeBatch();
        }
    }

    public static void createSchema(RegionsDatabase database) throws SQLException {

        String schema = """
                CREATE TABLE IF NOT EXISTS Regions (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(255) NOT NULL
                );

                CREATE TABLE IF NOT EXISTS RegionSections (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    region_id BIGINT NOT NULL,
                    section_type VARCHAR(50) NOT NULL,
                    section_order INT NOT NULL,
                    name VARCHAR(255) NOT NULL,
                    FOREIGN KEY (region_id) REFERENCES Regions(id)
                );

                CREATE TABLE IF NOT EXISTS CubicRegions (
                    id BIGINT PRIMARY KEY,
                    world_uuid CHAR(36) NOT NULL,
                    low_x DOUBLE NOT NULL,
                    low_y DOUBLE NOT NULL,
                    low_z DOUBLE NOT NULL,
                    high_x DOUBLE NOT NULL,
                    high_y DOUBLE NOT NULL,
                    high_z DOUBLE NOT NULL,
                    FOREIGN KEY (id) REFERENCES RegionSections(id)
                );

                CREATE TABLE IF NOT EXISTS WhitelistedEntities (
                    region_id BIGINT NOT NULL,
                    entity_uuid CHAR(36) NOT NULL,
                    FOREIGN KEY (region_id) REFERENCES Regions(id)
                );

                CREATE TABLE IF NOT EXISTS RegionFlags (
                    region_id BIGINT NOT NULL,
                    flag_name VARCHAR(255) NOT NULL,
                    flag_scope VARCHAR(50) NOT NULL,
                    FOREIGN KEY (region_id) REFERENCES Regions(id)
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

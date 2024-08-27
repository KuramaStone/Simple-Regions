package com.github.kuramastone.regionstrial.mysql;

import com.github.kuramastone.regionstrial.RegionAPI;
import com.github.kuramastone.regionstrial.RegionPlugin;
import com.github.kuramastone.regionstrial.regions.Region;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;

public class RegionsDatabase {

    private HikariConfig database;
    private String databaseName;

    public RegionsDatabase(String url, String databaseName, String user, String password, boolean useH2) {
        this.databaseName = databaseName;

        database = new HikariConfig();
        database.setJdbcUrl(url);
        database.setUsername(user);
        database.setPassword(password);
        database.setMaximumPoolSize(4);
        if (useH2)
            database.setDriverClassName("org.h2.Driver"); // Specify the H2 driver
        else
            database.setDriverClassName("com.mysql.cj.jdbc.Driver");
        database.setConnectionTimeout(30000L); // 30 seconds
        database.setDataSource(new HikariDataSource(database));

        try {
            initializeDatabase();
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Connection getConnection() throws SQLException {
        return database.getDataSource().getConnection();
    }

    public List<Region> loadRegions() {
        try {
            return RegionSQL.loadAllRegions(this);
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void saveRegions(Collection<Region> regions) {

        for (Region region : regions) {
            try {
                RegionSQL.saveRegion(this, region);
            }
            catch (Exception e) {
                RegionPlugin.logger.severe("Unable to save region into mysql database!");
                e.printStackTrace();
            }
        }

    }

    public void initializeDatabase() throws SQLException {
        // Create database
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            String createDatabaseSQL = "CREATE DATABASE IF NOT EXISTS %s".formatted(databaseName);
            statement.execute(createDatabaseSQL);

            statement.execute("USE " + databaseName);
        }

        RegionSQL.createSchema(this);
    }
}

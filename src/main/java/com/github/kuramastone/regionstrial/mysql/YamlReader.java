package com.github.kuramastone.regionstrial.mysql;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.representer.Representer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

/*
Custom wrapper for the yamlconfiguration to handle some behind the scenes logic.
Saves .yml from classpath if not on disk.
 */
public class YamlReader {

    private final JavaPlugin plugin;
    private final File yamlFile;
    private YamlConfiguration yamlData;

    public YamlReader(JavaPlugin plugin, String fileName) {
        this.plugin = plugin;
        this.yamlFile = new File(plugin.getDataFolder(), fileName);
        loadYaml();
        setYamlOptions();
    }

    public void installNewKeysFromDefault(String path) {
        String resourcePath = yamlFile.getName();
        boolean replace = false;
        if (resourcePath != null && !resourcePath.equals("")) {
            resourcePath = resourcePath.replace('\\', '/');

            InputStream in = plugin.getResource(resourcePath);
            if (in == null) {
                throw new IllegalArgumentException("The embedded resource '" + resourcePath + "' cannot be found.");
            }
            else {

                try {
                    boolean wasUpdated = false;
                    // try saving new keys if they exist in the jar but not the file
                    YamlConfiguration resYaml = YamlConfiguration.loadConfiguration(new InputStreamReader(in));

                    Set<String> keyList = path.isEmpty() || path == null ? resYaml.getKeys(false) : resYaml.getConfigurationSection(path).getKeys(false);

                    for (String key : keyList) {
                        // load each new key. if it doesnt exist in the file, save the whole section here

                        String keyPath = path + "." + key;
                        if (!this.yamlData.contains(keyPath)) {
                            wasUpdated = true;


                            this.yamlData.set(keyPath, resYaml.get(keyPath));
                        }

                    }

                    if(wasUpdated) {
                        plugin.getLogger().log(Level.INFO, "Updating config " + yamlFile.getName() + " with new settings!");
                    }

                    save();
                }
                catch (Exception var10) {
                    Exception ex = var10;
                    plugin.getLogger().log(Level.WARNING, "Could not update config " + yamlFile.getName() + " with new settings.", ex);
                }

            }
        }
        else {
            throw new IllegalArgumentException("ResourcePath cannot be null or empty");
        }
    }

    private void setYamlOptions() {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setIndent(4);
        Representer representer = new Representer(options);
        representer.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        // dont use parseComments() before 1.18. Use copyHeader for backwards compatibility
        this.yamlData.options().copyHeader(true);
        this.yamlData.options().indent(4);
    }

    public void set(String key, Object obj) {
        this.yamlData.set(key, obj);
    }

    public Set<String> getKeys(String key, boolean deep) {
        ConfigurationSection sec = this.yamlData.getConfigurationSection(key);
        return sec == null ? null : sec.getKeys(deep);
    }

    public boolean isSection(String key) {
        return yamlData.isConfigurationSection(key);
    }

    public boolean hasKey(String key) {
        return yamlData.contains(key);
    }

    public String getString(String key) {
        return yamlData.getString(key);
    }

    public Integer getInteger(String key) {
        Object value = yamlData.get(key);

        // auto convert
        if (value instanceof Double val)
            return val.intValue();
        if (value instanceof Float val)
            return val.intValue();

        return value instanceof Integer ? (Integer) value : null;
    }

    public Boolean getBoolean(String key) {
        return yamlData.getBoolean(key);
    }

    public Float getFloat(String key) {
        Object value = yamlData.get(key);

        // auto convert
        if (value instanceof Double val)
            return val.floatValue();
        if (value instanceof Integer val)
            return val.floatValue();

        return (Float) value;
    }

    public Double getDouble(String key) {
        Object value = yamlData.get(key);

        // auto convert
        if (value instanceof Float val)
            return val.doubleValue();
        if (value instanceof Integer val)
            return val.doubleValue();

        return (Double) value; // double loading is compatible with double and float
    }

    @SuppressWarnings("unchecked")
    public List<String> getStringList(String key) {
        Object value = yamlData.get(key);
        return value instanceof List ? (List<String>) value : null;
    }

    @SuppressWarnings("unchecked")
    public List<Boolean> getBooleanList(String key) {
        Object value = yamlData.get(key);
        return value instanceof List ? (List<Boolean>) value : null;
    }

    public void save() {
        try {
            yamlData.save(yamlFile);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadYaml() {

        if (!yamlFile.exists()) {
            plugin.saveResource(yamlFile.getName(), false); // load from classpath
        }

        yamlData = YamlConfiguration.loadConfiguration(yamlFile);

    }

    public YamlConfiguration data() {
        return yamlData;
    }

    public Object get(String id) {
        return yamlData.get(id);
    }
}
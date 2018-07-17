package org.exiledkingcc.java.cfgs;

import org.exiledkingcc.java.cfgs.ann.ConfigItem;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public enum ConfigManager {
    INSTANCE;

    private String defaultSource = "cp://";
    private HashMap<String, Object> configItems = new HashMap<>();
    private ConfigSourceFactory sourceFactory = new ConfigSourceFactory();
    private ConfigFormatFactory formatFactory = new ConfigFormatFactory();

    public void registerDefaultSource(String path) {
        this.defaultSource = path;
    }

    public <T> void registerConfig(String path, Class<T> clazz) {
        ConfigItem configItem = clazz.getAnnotation(ConfigItem.class);
        if (configItem == null) {
            throw new IllegalArgumentException(
                    "Class " + clazz.getName() + " is NOT a ConfigItem");
        }
        ConfigSource configSource = this.sourceFactory.getSource(path);
        if (configSource == null) {
            throw new IllegalArgumentException(
                    "Can NOT create ConfigSource for " + path);
        }
        String configName = configItem.name();
        if (this.configItems.containsKey(configName)) {
            String clzName = this.configItems.get(configName).getClass().getName();
            throw new IllegalArgumentException(
                    "Config " + configName + " has been registered with " + clzName);
        }
        ConfigFormat configFormat = this.formatFactory.getFormat(configName);
        if (configFormat == null) {
            throw new IllegalArgumentException(
                    "Can NOT create ConfigFormat for " + configName);
        }
        InputStream inputStream = configSource.getConfigData(configName);
        Map<String, Object> kv = configFormat.parseConfig(inputStream);
        T cfg = ConfigLoader.loadConfig(clazz, kv);
        this.configItems.put(configName, cfg);
    }

    public void registerConfig(Class<?> clazz) {
        this.registerConfig(this.defaultSource, clazz);
    }

    public <T> T getConfig(Class<T> clazz) {
        ConfigItem configItem = clazz.getAnnotation(ConfigItem.class);
        if (configItem == null) {
            throw new IllegalArgumentException(
                    "Class " + clazz.getName() + " is NOT a ConfigItem");
        }
        return (T)this.configItems.get(configItem.name());
    }

}

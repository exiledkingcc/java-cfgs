package org.exiledkingcc.java.cfgs;

import org.exiledkingcc.java.cfgs.ann.ConfigItem;
import org.exiledkingcc.java.cfgs.ann.ConfigKey;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
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
        T cfg = this.loadConfig(clazz, kv);
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

    private <T> T loadConfig(Class<T> clazz, Map<String, Object> kv) {
        T cfg;
        try {
            cfg = clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Can NOT create " + clazz.getName(), e);
        }
        for (Field field : clazz.getDeclaredFields()) {
            ConfigKey configKey = field.getAnnotation(ConfigKey.class);
            if (configKey == null) {
                continue;
            }
            String key = configKey.key();
            boolean optional = configKey.optional();
            Object value = kv.getOrDefault(key, null);
            if (value == null) {
                if (!optional) {
                    throw new RuntimeException("Required key <" + key + "> is missing");
                }
            } else {
                this.setConfigValue(cfg, field, value, configKey);
            }
        }
        return cfg;
    }

    private <T> void setConfigValue(T cfg, Field field, Object value, ConfigKey ck) {
        boolean accessible = field.isAccessible();
        try {
            field.setAccessible(true);
            Class type = field.getType();
            if (type.equals(Boolean.TYPE)) {
                value = ConfigValueCast.toBool(value);
            } else if (type.equals(Integer.TYPE) || type.equals(Long.TYPE)) {
                value = ConfigValueCast.toInteger(value);
            } else if (type.equals(Float.TYPE) || type.equals(Double.TYPE)) {
                value = ConfigValueCast.toFloat(value);
            } else if (type.equals(String.class)) {
                // do nothing
            } else if (type.equals(Map.class)) {
                Class itemType = ck.itemType();
                if (!itemType.equals(Void.TYPE)) {
                    if (!(value instanceof Map)) {
                        throw new RuntimeException("Filed " + field.getName() +
                                " is NOT configured with map values");
                    }
                    Map<String, Object> itemKV = (Map<String, Object>)value;
                    for (Map.Entry<String, Object> entry: itemKV.entrySet()) {
                        Map<String, Object> mm = (Map<String, Object>) entry.getValue();
                        entry.setValue(this.loadConfig(itemType, mm));
                    }
                    value = itemKV;
                }
            } else if (type.equals(List.class)) {
                Class itemType = ck.itemType();
                if (!itemType.equals(Void.TYPE)) {
                    if (!(value instanceof List)) {
                        throw new RuntimeException("Filed " + field.getName() +
                                " is NOT configured with list values");
                    }
                    List<Object> itemList = (List<Object>)value;
                    for (int i = 0; i < itemList.size(); ++i) {
                        Map<String, Object> mm = (Map<String, Object>) itemList.get(i);
                        if (mm == null) {
                            continue;
                        }
                        itemList.set(i, this.loadConfig(itemType, mm));
                    }
                    value = itemList;
                }
            } else {
                Map<String, Object> mm = (Map<String, Object>) value;
                value = this.loadConfig(field.getType(), mm);
            }
            field.set(cfg, value);
            field.setAccessible(accessible);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Can NOT set field " + field.getName(), e);
        } finally {
            field.setAccessible(accessible);
        }
    }

    private static class ConfigValueCast {
        static Object toBool(Object value) {
            if (value instanceof Boolean) {
                return value;
            } else if (value instanceof String) {
                String v = (String)value;
                if ("true".equalsIgnoreCase(v)) {
                    return Boolean.TRUE;
                } else if ("false".equalsIgnoreCase(v)) {
                    return Boolean.FALSE;
                }
            }
            throw new RuntimeException("Value <" + value + "> can NOT cast to Boolean");
        }

        static Object toInteger(Object value) {
            if (value instanceof Integer) {
                return value;
            } else if (value instanceof String) {
                return Integer.parseInt((String) value);
            } else if (value instanceof Number) {
                return ((Number)value).intValue();
            }
            throw new RuntimeException("Value <" + value + "> can NOT cast to Integer");
        }

        static Object toFloat(Object value) {
            if (value instanceof Integer) {
                return value;
            } else if (value instanceof String) {
                return Float.parseFloat((String)value);
            } else if (value instanceof Number) {
                return ((Number)value).floatValue();
            }
            throw new RuntimeException("Value <" + value + "> can NOT cast to Float");
        }
    }

}

package org.exiledkingcc.java.cfgs;

import org.exiledkingcc.java.cfgs.ann.ConfigKey;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public class ConfigLoader {

    static  <T> T loadConfig(Class<T> clazz, Map<String, Object> kv) {
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
                setConfigValue(cfg, field, value, configKey);
            }
        }
        return cfg;
    }

    private static  <T> void setConfigValue(T cfg, Field field, Object value, ConfigKey ck) {
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
                        entry.setValue(loadConfig(itemType, mm));
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
                        itemList.set(i, loadConfig(itemType, mm));
                    }
                    value = itemList;
                }
            } else {
                Map<String, Object> mm = (Map<String, Object>) value;
                value = loadConfig(field.getType(), mm);
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

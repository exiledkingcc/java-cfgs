package org.exiledkingcc.java.cfgs;

import org.exiledkingcc.java.cfgs.format.PropertiesFormat;

import java.util.HashMap;

public class ConfigFormatFactory {

    private HashMap<String, ConfigFormat> configFormatMap = new HashMap<>();

    ConfigFormat getFormat(String name) {
        if (this.configFormatMap.containsKey(name)) {
            return this.configFormatMap.get(name);
        }
        ConfigFormat configFormat = null;
        if (name.endsWith(".properties")) {
            configFormat = new PropertiesFormat();
        }
        if (configFormat != null) {
            this.configFormatMap.put(name, configFormat);
        }
        return configFormat;
    }
}

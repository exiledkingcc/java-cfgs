package org.exiledkingcc.java.cfgs;

import org.exiledkingcc.java.cfgs.source.ClassPathSource;
import org.exiledkingcc.java.cfgs.source.FileSystemSource;

import java.util.HashMap;

public class ConfigSourceFactory {

    private HashMap<String, ConfigSource> configSourceMap = new HashMap<>();

    public ConfigSource getSource(String path) {
        if (this.configSourceMap.containsKey(path)) {
            return this.configSourceMap.get(path);
        }
        int p = path.indexOf("://");
        if (p < 0) {
            throw new IllegalArgumentException(path + " has NO scheme");
        }
        String scheme = path.substring(0, p);
        ConfigSource configSource = getSource(scheme, path);
        if (configSource == null) {
            throw new IllegalArgumentException("No source found for path: " + path);
        }
        this.configSourceMap.put(path, configSource);
        return configSource;
    }

    private ConfigSource getSource(String scheme, String path) {
        if (scheme.equals("cp")) {
            return new ClassPathSource();
        } else if (scheme.equals("file")) {
            return new FileSystemSource(path);
        }
        return null;
    }
}

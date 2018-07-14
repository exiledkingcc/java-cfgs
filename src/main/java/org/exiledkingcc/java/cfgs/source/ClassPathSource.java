package org.exiledkingcc.java.cfgs.source;

import org.exiledkingcc.java.cfgs.ConfigSource;

import java.io.InputStream;

public class ClassPathSource implements ConfigSource {

    @Override
    public InputStream getConfigData(String name) {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(name);
        if (inputStream == null) {
            throw new IllegalArgumentException("No ConfigData found for " + name);
        }
        return inputStream;
    }
}

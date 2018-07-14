package org.exiledkingcc.java.cfgs.source;

import org.exiledkingcc.java.cfgs.ConfigSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class FileSystemSource implements ConfigSource {

    private final File dir;

    public FileSystemSource(String path) {
        // remove "file://"
        path = path.substring(7);
        this.dir = new File(path);
        if (!this.dir.exists() || !this.dir.isDirectory()) {
            throw new IllegalArgumentException("Path <" + path +
                    "> is NOT exist or is NOT a directory");
        }
    }

    @Override
    public InputStream getConfigData(String name) {
        File cfgFile = new File(this.dir, name);
        try {
            return new FileInputStream(cfgFile);
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("No ConfigData found for " + name, e);
        }
    }
}

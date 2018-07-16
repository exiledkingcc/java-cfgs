package org.exiledkingcc.java.cfgs.source;

import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.exiledkingcc.java.cfgs.ConfigSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ZookeeperSource implements ConfigSource {

    private final int SESSION_TIME_OUT = 60000; // 60s
    private final ZooKeeper zooKeeper;
    private final Watcher watcher = watchedEvent -> {
        // do nothing...
    };

    public ZookeeperSource(String path) {
        path = path.substring(5);
        byte[] auth = null;
        if (path.contains("@")) {
            int p = path.indexOf('@');
            auth = path.substring(0, p).getBytes();
            path = path.substring(p + 1);
        }
        try {
            this.zooKeeper = new ZooKeeper(path, SESSION_TIME_OUT, this.watcher);
            if (auth != null) {
                this.zooKeeper.addAuthInfo("digest", auth);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Can NOT connect to zookeeper: " + path);
        }
    }

    @Override
    public InputStream getConfigData(String name) {
        try {
            byte[] data = this.zooKeeper.getData("/" + name, true, null);
            return new ByteArrayInputStream(data);
        } catch (Exception e) {
            throw  new RuntimeException("Can NOT get config for <" + name + "> from zookeeper", e);
        }
    }
}

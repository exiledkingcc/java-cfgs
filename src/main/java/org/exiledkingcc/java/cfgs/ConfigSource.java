package org.exiledkingcc.java.cfgs;

import java.io.InputStream;

public interface ConfigSource {

    InputStream getConfigData(String name);
}

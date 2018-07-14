package org.exiledkingcc.java.cfgs;

import java.io.InputStream;
import java.util.Map;

public interface ConfigFormat {

    Map<String, Object> parseConfig(InputStream inputStream);
}

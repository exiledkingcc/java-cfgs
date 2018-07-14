package org.exiledkingcc.java.cfgs.format;

import org.exiledkingcc.java.cfgs.ConfigFormat;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class PropertiesFormat implements ConfigFormat {

    @Override
    public Map<String, Object> parseConfig(InputStream inputStream) {
        Properties properties = new Properties();
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("parseConfig ERROR", e);
        }
        HashMap<String, Object> kv = new HashMap<>();
        for (String key: properties.stringPropertyNames()) {
            String value = properties.getProperty(key);
            kv.put(key, value);
        }
        kv = translate(kv);
        return kv;
    }

    private HashMap<String, Object> translate(Map<String, Object> kv) {
        HashMap<String, Object> hashMap = new HashMap<>();
        for (String key: kv.keySet()) {
            Object value = kv.get(key);
            int p = key.indexOf('.');
            if (p < 0) {
                hashMap.put(key, value);
            } else {
                String k = key.substring(0, p);
                String kk = key.substring(p + 1);
                if (hashMap.containsKey(k)) {
                    ((Map<String, Object>)hashMap.get(k)).put(kk, value);
                } else {
                    HashMap<String, Object> hh = new HashMap<>();
                    hh.put(kk, value);
                    hashMap.put(k, hh);
                }
            }
        }
        for (Map.Entry<String, Object> entry: hashMap.entrySet()) {
            Object obj = entry.getValue();
            if (obj instanceof Map) {
                Map<String, Object> mm = (Map<String, Object>) obj;
                mm = this.translate((Map<String, Object>) obj);
                boolean isList = false;
                boolean notList = false;
                for (String k: mm.keySet()) {
                    if (k.matches("\\$[0-9]+")) {
                        isList = true;
                    } else {
                        notList = true;
                    }
                }
                if (isList == notList) {
                    throw new RuntimeException(
                            "Notation '$' used to be define list, mismatch for " + entry.getKey());
                }
                if (isList) {
                    entry.setValue(mapToList(mm));
                } else {
                    entry.setValue(mm);
                }
            }
        }
        return hashMap;
    }

    private List<Object> mapToList(Map<String, Object> map) {
        ArrayList<Object> arrayList = new ArrayList<>();
        for (String key: map.keySet()) {
            assert key.charAt(0) == '$';
            int idx = Integer.parseInt(key.substring(1));
            arrayList.add(idx, map.get(key));
        }
        return arrayList;
    }
}

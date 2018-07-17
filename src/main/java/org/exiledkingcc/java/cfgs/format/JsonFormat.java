package org.exiledkingcc.java.cfgs.format;

import com.google.gson.*;
import org.exiledkingcc.java.cfgs.ConfigFormat;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class JsonFormat implements ConfigFormat {

    @Override
    public Map<String, Object> parseConfig(InputStream inputStream) {
        JsonParser jsonParser = new JsonParser();
        JsonElement jsonElement = jsonParser.parse(new InputStreamReader(inputStream));
        HashMap<String, Object> kv = translate(jsonElement.getAsJsonObject());
        return kv;
    }

    private static HashMap<String, Object> translate(JsonObject jsonObject) {
        HashMap<String, Object> hashMap = new HashMap<>();
        for (String key: jsonObject.keySet()) {
            JsonElement element = jsonObject.get(key);
            Object value = translate(element);
            hashMap.put(key, value);
        }
        return hashMap;
    }

    private static List<Object> translate(JsonArray jsonArray) {
        ArrayList<Object> arrayList = new ArrayList<>();
        for (JsonElement element: jsonArray) {
            Object value = translate(element);
            arrayList.add(value);
        }
        return arrayList;
    }

    private static Object translate(JsonElement element) {
        Object value = null;
        if (element.isJsonObject()) {
            value = translate(element.getAsJsonObject());
        } else if (element.isJsonArray()) {
            value = translate(element.getAsJsonArray());
        } else if (element.isJsonPrimitive()) {
            JsonPrimitive jsonPrimitive = element.getAsJsonPrimitive();
            if (jsonPrimitive.isBoolean()) {
                value = jsonPrimitive.getAsBoolean();
            } else if (jsonPrimitive.isString()) {
                value = jsonPrimitive.getAsString();
            } else if (jsonPrimitive.isNumber()) {
                value = jsonPrimitive.getAsNumber();
            } else {
                throw new UnsupportedOperationException("Type <" + jsonPrimitive.getClass().getSimpleName() +
                        "> is NOT supported");
            }
        }
        return value;
    }

}

package org.hunters.server.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

public class Json {
    private Map<String, String> jsonObj;
    public Json() {
        this.jsonObj = new HashMap();
    }
    public Json(Map<String, String> map) {
        this.jsonObj = map;
    }
    public Json(String data) {
        this.jsonObj = new HashMap();
        Matcher matcher = Parsers.jsonPattern.matcher(data);
        while(matcher.find()) {
            this.jsonObj.put(matcher.group(1), matcher.group(2));
        }
    }

    public String get(String key) {
        return jsonObj.get(key);
    }

    public String put(String key, String value) {
        return jsonObj.put(key, value);
    }

    public boolean hasKey(String key) { return jsonObj.containsKey(key); }

    public static Json parseJSON(String data) {
        if(data == null)
            return null;
        var parsed = new Json(data);
        if(parsed.getSize() == 0)
            return null;
        return parsed;
    }

    public static Json[] parseJsonArray(String data) {
        if(data == null)
            return null;
        int offset = data.charAt(0) == '[' ? 1 : 0;
        int end    = data.charAt(data.length()-1) == ']' ? data.length()-2 : data.length()-1;
        String[] swipes = data.substring(offset+1, end).split("},\\{");
        Json[] jsonArr = new Json[swipes.length];
        for (int i = 0; i < swipes.length; i++)
            jsonArr[i] = Json.parseJSON(swipes[i]);
        return jsonArr;
    }

    public int getSize() { return jsonObj.size(); }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("{");
        for(var it : jsonObj.entrySet()) {
            s.append('\"' + it.getKey() + "\":\"" + it.getValue() + "\",");
        }
        if(s.length() > 1)
            s.setCharAt(s.length()-1, '}');
        else
            s.append('}');
        return s.toString();
    }

    public String remove(String key) {
        return this.jsonObj.remove(key);
    }

    public Map<String, String> asMap() {
        return jsonObj;
    }
}

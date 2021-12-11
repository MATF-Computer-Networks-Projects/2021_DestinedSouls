package server.utils;

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

    public static Json parseJSON(String data) {
        return new Json(data);
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("{");
        for(var it : jsonObj.entrySet()) {
            s.append('\"' + it.getKey() + "\":\"" + it.getValue() + "\",");
        }
        s.setCharAt(s.length()-1, '}');
        return s.toString();
    }
}

package org.hunters.server.protocols.http;

import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    public HttpHeaders         headers;
    public Object              payload = null;
    private Map<String, String> other = null;

    public String getValue(String key) {
        if(!hasValue(key))
            return null;
        return other.get(key);
    }

    public void setValue(String key, String value) {
        if(this.other == null)
            this.other = new HashMap<>();
        this.other.put(key, value);
    }

    public boolean hasValue(String key) {
        return (other != null) && other.containsKey(key);
    }
}

package org.hunters.server.http;

import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    public HttpHeaders         headers;
    public Object              payload = null;
    public Map<String, String> other = new HashMap<>();
}

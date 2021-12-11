package server.utils;

import java.util.Map;

public class Response {
    public int status;
    public String json;
    public Response(int status, String json) {
        this.status = status;
        this.json = json;
    }

    public Response(String json) {
        this.status = 200;
        this.json = json;
    }
}

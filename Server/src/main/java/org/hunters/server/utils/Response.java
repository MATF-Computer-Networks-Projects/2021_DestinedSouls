package org.hunters.server.utils;

public class Response {
    public int status;
    public Json json;

    public Response(int status, Json json) {
        this.status = status;
        this.json = json;
    }

    public Response(int status, String json) {
        this(status, Json.parseJSON(json));
    }

    public Response(int status) {
        this.status = status;
        this.json = null;
    }
}

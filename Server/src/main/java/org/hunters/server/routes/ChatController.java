package org.hunters.server.routes;

import org.hunters.server.protocols.http.HttpRequest;
import org.hunters.server.utils.Json;
import org.hunters.server.utils.Response;

public class ChatController implements IController{
    @Override
    public Response handle(Object request) {
        if(request instanceof HttpRequest)
            return onHttpHandle((HttpRequest)request);

        return new Response(501);
    }

    private Response onHttpHandle(HttpRequest request) {
        var headers = request.headers;
        if (headers.ws != null && headers.ws.key != null) {
            if (headers.ws.protocol != null) {
                if (!headers.ws.protocol.contains("chat"))
                    headers.ws.protocol = "chat";
                else
                    headers.ws.protocol = headers.ws.protocol.split(",", 2)[0];

            }
            return new Response(101, Json.parseJSON("{\"key\":\"" + headers.ws.key + "\"}"));
        }
        return new Response(400, Json.parseJSON("{\"msg\":\"Websocket headers missing!\"}"));
    }
}

package org.hunters.server.routes;

import org.hunters.server.protocols.http.HttpRequest;
import org.hunters.server.protocols.ws.framing.CloseFrame;
import org.hunters.server.security.Authorizer;
import org.hunters.server.services.UserService;
import org.hunters.server.services.Validator;
import org.hunters.server.utils.Json;
import org.hunters.server.utils.Response;

public class ChatController implements IController{
    @Override
    public Response handle(Object request) {
        if(request instanceof HttpRequest)
            return onHttpHandle((HttpRequest)request);

        // return new Response(501);
        return onSendMessage((String) request);
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
            Json token = Json.parseJSON(Authorizer.parseToken(headers.token));
            if(token == null)
                return new Response(403);
            int id = Integer.parseInt(token.get("sub"));
            UserService.setSocketId(id, headers.ws.socketId);

            return new Response(101, Json.parseJSON("{\"key\":\"" + headers.ws.key + "\"}"));
        }
        return new Response(400, Json.parseJSON("{\"msg\":\"Websocket headers missing!\"}"));
    }

    private final String[] msgSchema = new String[]{"token", "id", "msg", "socketId"};
    private Response onSendMessage(String request) {
        Json payload = Json.parseJSON(request);
        if(payload == null || !Validator.validateSchema(payload, msgSchema))
            return new Response(CloseFrame.REFUSE);

        System.out.println("Ws message: " + payload);

        Json token = Json.parseJSON(Authorizer.parseToken(payload.get("token")));
        if(token == null)
            return new Response(1008);

        int id = Integer.parseInt(token.get("sub"));
        int chatId = Integer.parseInt(payload.get("id"));

        int userId = UserService.getMatchUser(chatId, id);
        if(userId < 0)
            return new Response(1008);

        //UserService.setSocketId(id, Integer.parseInt(payload.get("socketId")));

        // payload.put("id", String.valueOf(userId));
        long targetSocketId = UserService.getSocketId(userId);
        if(targetSocketId == -1)
            UserService.appendMessage(userId, chatId, payload.get("msg"));
        else
            payload.put("socketId", Long.toString(targetSocketId));

        payload.remove("token");
        return new Response(0, payload);
    }
}

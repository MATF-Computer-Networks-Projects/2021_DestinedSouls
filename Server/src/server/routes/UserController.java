package server.routes;

import server.services.UserService;
import server.utils.Json;
import server.utils.Parsers;
import server.utils.Response;

import java.nio.channels.SelectionKey;
import java.util.List;
import java.util.Map;

public class UserController {
    public static Response get(String url) {
        switch (url) {
            case "":
            case "/": return null;
            default: return new Response(501, null);
        }
    }

    public static Response post(String url, String data) {
        Json reqBody = new Json(data);

        switch (url) {
            case "/users/authenticate": return UserService.authenticate(
                                                    reqBody.get("email"),
                                                    reqBody.get("password")
                                                );
            case "/users/register": return new Response(501, null);
            default: return new Response(404, null);
        }
    }
}

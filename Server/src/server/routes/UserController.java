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
        System.out.println("User controller " + url);
        switch (url) {
            case "":
            case "/": return null;
            default: return new Response(501, null);
        }
    }

    public static Response post(String url, String data) {
        Json reqBody = new Json(data);
        System.out.println("User controller: " + url + "\r\n" + reqBody);

        switch (url) {
            case "authenticate": return UserService.authenticate(
                                                    reqBody.get("email"),
                                                    reqBody.get("password")
                                                );
            case "register": return new Response(501, null);
            default: return new Response(404, null);
        }
    }
}

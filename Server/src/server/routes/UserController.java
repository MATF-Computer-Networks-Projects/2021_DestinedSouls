package server.routes;

import server.middleware.Validator;
import server.models.users.User;
import server.services.UserService;
import server.utils.Json;
import server.utils.Parsers;
import server.utils.Response;

import java.nio.channels.SelectionKey;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class UserController {
    public static Response get(String url) {
        return get(url, null);
    }
    public static Response get(String url, Integer id) {
        System.out.println("User controller " + url);
        switch (url) {
            case "":
            case "/": return null;
            case "getAll": {
                if(id == null) { return new Response(403, null); }
                var users = UserService.getAll(id);
                if(users == null) { return new Response(403, null); }
                StringBuilder sb = new StringBuilder("[");
                for(var u : users)
                    sb.append(u).append(',');
                sb.append(']');
                return new Response(200, sb.toString());
            }
            default: return new Response(501, null);
        }
    }

    public static Response post(String url, Json reqBody) {
        //System.out.println("User controller: " + url + "\r\n" + data);
        // Json reqBody = new Json(data);
        System.out.println("User controller: " + url + "\r\n" + reqBody);

        switch (url) {
            case "authenticate": {
                var err = catchError(reqBody, new String[]{"email", "password"});
                if(err != null)
                    return err;

                return UserService.authenticate(
                        reqBody.get("email"),
                        reqBody.get("password")
                );
            }
            case "register": {
                var err = catchError(reqBody, new String[]{"name", "birthday", "gender",
                                                                    "interest", "email", "password"});
                if(err != null)
                    return err;

                return UserService.register(reqBody);
            }
            default: return new Response(404, null);
        }
    }

    private static Response catchError(Json reqBody, String[] options) {
        if(Validator.validateSchema(reqBody, options))
            return null;
        return new Response(400, "Missing option \"" + reqBody.get("error") + '\"');
    }
}

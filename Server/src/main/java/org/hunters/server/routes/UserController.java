package org.hunters.server.routes;

import org.hunters.server.protocols.http.HttpRequest;
import org.hunters.server.security.Authorizer;
import org.hunters.server.services.UserService;
import org.hunters.server.services.Validator;
import org.hunters.server.utils.Json;
import org.hunters.server.utils.Response;

import java.util.Arrays;

public class UserController implements IController {
    private static final String routeRoot = "/users";
    public UserController() {
    }

    @Override
    public Response handle(Object request) {
        return handleHttp((HttpRequest) request);
    }

    private Response handleHttp(HttpRequest request) {
        switch (request.headers.httpMethod) {
            case GET:  { return get(request); }
            case POST: { return post(request); }
            default: {    return new Response(405); }
        }
    }

    private Response get(HttpRequest request) {
        switch (request.headers.url) {
            case "/swipes": {
                if(request.headers.token == null)
                    return new Response(401);

                Json token = Json.parseJSON(Authorizer.parseToken(request.headers.token));

                if(token == null)
                    return new Response(401);


                int id = Integer.parseInt(token.get("sub"));
                if(id <= 0) { return new Response(403); }
                var users = UserService.getSwipes(id);
                if(users == null) { return new Response(403); }
                StringBuilder sb = new StringBuilder("[");
                for(var u : users)
                    sb.append(u).append(',');
                sb.setCharAt(sb.length()-1, ']');
                System.out.println(sb);

                Json jsonArray = new Json();
                jsonArray.put("jsonArray", sb.toString());

                return new Response(200, jsonArray);
            }
            default: return new Response(501);
        }
    }

    private Response post(HttpRequest request) {
        switch (request.headers.url) {
            case "/authenticate": {
                Json reqBody = (Json)request.payload;
                if(!Validator.validateSchema(reqBody, new String[]{"email", "password"}))
                    return new Response(400, "\"msg:\":\"" + reqBody.get("error") + "\"");


                return UserService.authenticate(reqBody.get("email"), reqBody.get("password"));
            }

            case "/register": {
                Json reqBody = (Json)request.payload;
                if(!Validator.validateSchema(reqBody, new String[]{"name", "birthday", "gender",
                        "interest", "email", "password"})) {
                    return new Response(400, "\"msg:\":\" Missing key: \"" + reqBody.get("error") + "\"");
                }

                return UserService.register(reqBody);
            }

            case "/swipes": {
                Json token = Json.parseJSON(Authorizer.parseToken(request.headers.token));
                if(token == null)
                    return new Response(401);
                int id = Integer.parseInt(token.get("sub"));

                Json[] reqBody = Json.parseJsonArray((String) request.payload);

                for(var json : reqBody) {
                    if(!Validator.validateSchema(json, new String[]{"id", "like"}))
                        return new Response(400, "{\"msg:\":\" Missing key: \"" + json.get("error") + "\"}");
                    UserService.handleSwipeVote(id, Integer.parseInt(json.get("id")), json.get("like").equals("true"));
                }
                return new Response(200, new Json());
            }
            default: return new Response(501);
        }
    }
}

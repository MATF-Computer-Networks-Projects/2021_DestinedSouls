package server.routes;

import server.Message;
import server.http.HttpHeaders;
import server.security.Authorizer;
import server.services.UserService;
import server.services.Validator;
import server.utils.FileInfo;
import server.utils.Json;
import server.utils.Response;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class UserController implements IController {
    private static final String routeRoot = "/users";
    public UserController() {
    }

    @Override
    public void get(Message request, Message response) {
        var httpMeta = (HttpHeaders)request.metaData;
        String url = httpMeta.url.substring(routeRoot.length());

        switch (url) {
            case "/getAll": {
                if(httpMeta.token == null) {
                    response.writeToMessage(Router.cache.get("401").duplicate());
                    return;
                }
                Json token = new Json(Authorizer.parseToken(httpMeta.token));

                if(token == null) {
                    response.writeToMessage(Router.cache.get("401").duplicate());
                    return;
                }

                int id = Integer.parseInt(token.get("sub"));
                if(id <= 0) { response.writeToMessage(Router.cache.get("403").duplicate()); return; }
                var users = UserService.getAll(id);
                if(users == null) { response.writeToMessage(Router.cache.get("403").duplicate()); return; }
                StringBuilder sb = new StringBuilder("[");
                for(var u : users)
                    sb.append(u).append(',');
                sb.setCharAt(sb.length()-1, ']');
                System.out.println(sb);

                response.writeToMessage(Router.cache.createResponseBuffer(FileInfo.json(
                        StandardCharsets.UTF_8, sb.toString().getBytes())));
                return;
            }
            default: response.writeToMessage(Router.cache.get("501").duplicate());
        }
    }

    @Override
    public void post(Message request, Message response) {
        var httpMeta = (HttpHeaders)request.metaData;
        String url = httpMeta.url.substring(routeRoot.length());

        Json reqBody = new Json( new String( Arrays.copyOfRange(request.sharedArray,
                                                    httpMeta.bodyStartIndex,
                                                    httpMeta.bodyEndIndex) )
                                );


        switch (url) {
            case "/authenticate": {
                if(!Validator.validateSchema(reqBody, new String[]{"email", "password"})) {
                    response.writeToMessage(Router.cache.createBadRequest(reqBody.get("error")).duplicate());
                    return;
                }

                Response res = UserService.authenticate(reqBody.get("email"), reqBody.get("password"));
                if(res.status == 404) {
                    response.writeToMessage(Router.cache.get("404").duplicate());
                    return;
                }

                response.writeToMessage(Router.cache.createResponseBuffer(FileInfo.json(
                                                                        StandardCharsets.UTF_8, res.json.getBytes())));
                break;
            }

            case "/register": {
                if(!Validator.validateSchema(reqBody, new String[]{"name", "birthday", "gender",
                        "interest", "email", "password"})) {
                    response.writeToMessage(Router.cache.get("400").duplicate());
                    return;
                }

                Response res = UserService.register(reqBody);
                response.writeToMessage(Router.cache.createResponseBuffer(FileInfo.json(
                                                                    StandardCharsets.UTF_8, res.json.getBytes())));
                break;
            }
            default: response.writeToMessage(Router.cache.get("501").duplicate());
        }


    }
}

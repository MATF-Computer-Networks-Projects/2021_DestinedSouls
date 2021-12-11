package server.routes;

import server.utils.FileInfo;
import server.utils.Json;
import server.utils.Response;

public class RouteController {

    public static Response get(String url, Integer id) {
        if(FileInfo.isValid(url) || url.isEmpty())
            return ResourceController.get(url);

        var urlComps = url.split("/", 2);
        switch (urlComps[0]) {
            case "users": return UserController.get(urlComps[1], id);
            default: return new Response(404, null);
        }
    }
    public static Response get(String url) {
        return RouteController.get(url, null);
    }

    public static Response post(String url, Json body) {
        var urlComps = url.split("/", 2);
        System.out.println("Url: " + url);
        System.out.println("Controller: " + urlComps[1]);
        switch (urlComps[0]) {
            case "users": return UserController.post(urlComps[1], body);
            default: return new Response(404, null);
        }
    }
}

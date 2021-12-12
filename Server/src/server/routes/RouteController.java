package server.routes;

import server.middleware.StorageHandler;
import server.utils.FileInfo;
import server.utils.Json;
import server.utils.Response;

import java.nio.charset.StandardCharsets;

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

    public static Response post(String url, String body) {
        var urlComps = url.split("/", 2);
        //System.out.println("Url: " + url);
        //System.out.println("Controller: " + urlComps[1]);
        // System.out.print(body);
        switch (urlComps[0]) {
            case "upload": {
                String filename = body.substring(body.indexOf("filename=\"")+10, body.indexOf("Content-Type:")-3);
                // body = body.substring(body.indexOf("Content-Type:"));
                body = body.substring(body.indexOf("\r\n\r\n"));

                // System.out.println("Filename: " + filename);
                //return ResourceController.post(filename, body);
                return ResourceController.post(filename, body);
            }
            case "users": return UserController.post(urlComps[1], Json.parseJSON(body));
            default: return new Response(404, null);
        }
    }
}

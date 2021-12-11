package server.routes;

import server.Router;
import server.utils.FileInfo;
import server.utils.Response;
import server.utils.Responses;


public class ResourceController {

    public static Response get(String url) {
        String filename = FileInfo.getFilename(url);
        if(Router.responseBuffers.contains(filename)) {
            System.out.println("Resource controller: " + filename);
            return new Response(200, filename);
        }


        System.err.println("Resource controller: no filename " + filename);
        return new Response(404, "404");
    }

}

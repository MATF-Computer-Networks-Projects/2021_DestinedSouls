package server.routes;

import server.Message;
import server.http.HttpHeaders;
import server.utils.FileInfo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;


public class ResourceController implements IController {

    @Override
    public void get(Message request, Message response) {
        String url = ((HttpHeaders)request.metaData).url;
        String filename = FileInfo.getFilename(url);
        if (Router.cache.contains(filename)) {
            System.out.println("Resource controller: " + filename);
            response.writeToMessage(Router.cache.get(filename));
            return;
        } else {
            var path = Paths.get(FileInfo.PUBLIC_HTML_DIR, url);
            if (Files.isRegularFile(path)) {
                try {
                    response.writeToMessage( Router.cache.createResponseBuffer(
                                                            FileInfo.get(path, StandardCharsets.UTF_8))
                                            );
                    return;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        response.writeToMessage( Router.cache.get("404") );
    }

    @Override
    public void post(Message request, Message response) {
        response.writeToMessage( Router.cache.get("501") );
    }

    /*

        System.err.println("Resource controller: no filename " + filename);
        return new Response(404, "404");
    }

    public static Response post(String filename, String data) {
        System.out.println("Filename: " + filename);
        // System.out.println("Data: " + data);


        //System.exit(1);
        return new Response(200, "{\"msg\":\"Uploaded\"}");
    }
     */
}

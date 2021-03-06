package org.hunters.server.routes;

import org.hunters.server.protocols.http.HttpRequest;
import org.hunters.server.security.Authorizer;
import org.hunters.server.services.StorageService;
import org.hunters.server.services.UserService;
import org.hunters.server.utils.FileInfo;
import org.hunters.server.utils.Response;
import org.hunters.server.utils.Responses;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public class ResourceController implements IController {

    @Override
    public Response handle(Object request) {
        return handleHttp((HttpRequest) request);
    }

    private Response handleHttp(HttpRequest request) {
        switch (request.headers.httpMethod) {
            case GET:  {
                if(request.headers.token == null)
                    return get(request.headers.url);
            }
            case POST: { return post(request); }
            default: {    return new Response(405); }
        }
    }

    static Response get(String url) {
        if(url.startsWith("/profile"))
            return get(StorageService.uploadsDir.toString(), url.substring(url.lastIndexOf('/')+1));
        else
            return get(FileInfo.PUBLIC_HTML_DIR, FileInfo.getFilename(url));
    }

    static Response get(String parent, String filename) {
        if (!StorageService.cache.contains(filename)) {
            var path = Paths.get(parent, filename);
            if (Files.isRegularFile(path)) {
                try {
                    StorageService.cache.put(
                            filename,
                            Responses.createResponseBuffer(FileInfo.get(path, StandardCharsets.UTF_8))
                    );
                } catch (IOException e) {
                    return new Response(405);
                }
            }
        }

        return new Response(200, "{\"filename\":\"" + filename + "\"}");
    }


    static Response post(HttpRequest request) {
        if(request.headers.token == null)
            return new Response(401);

        var tokenParsed = Authorizer.authorize(request.headers.token);
        if(tokenParsed == null)
            return new Response(401);

        try {
            int id = Integer.parseInt(tokenParsed.get("sub"));

            if(!request.hasValue("filename"))
                return new Response(400);

            String filename = request.getValue("filename");


            Path imgPath = StorageService.store((byte[])request.payload, filename);
            return UserService.addImage(id, imgPath);

        } catch (NumberFormatException e) {
            return new Response(401);
        } catch (IOException e) {
            e.printStackTrace();
            return new Response(500);
        }

    }
}

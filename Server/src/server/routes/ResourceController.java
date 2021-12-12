package server.routes;

import server.Router;
import server.Server;
import server.utils.FileInfo;
import server.utils.Response;
import server.utils.Responses;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.awt.image.renderable.RenderContext;
import java.awt.image.renderable.RenderableImageOp;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


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

    public static Response post(String filename, String data) {
        System.out.println("Filename: " + filename);
        // System.out.println("Data: " + data);


        //System.exit(1);
        return new Response(200, "{\"msg\":\"Uploaded\"}");
    }
}

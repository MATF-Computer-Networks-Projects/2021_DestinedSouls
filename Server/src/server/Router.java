package server;

import server.middleware.Authorizer;
import server.models.users.User;
import server.routes.ResourceController;
import server.routes.RouteController;
import server.routes.UserController;
import server.services.UserService;
import server.utils.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.regex.Matcher;

final public class Router {

    public static Responses responseBuffers = new Responses(Server.PUBLIC_HTML_DIR);

    public static void httpRequestHandle(SelectionKey key, String req)
    {

        Response res = null;
        String[] reqComps = req.split(" ", 3);
        String url = reqComps[1].substring(1);
        System.out.println("Url: " + url);



        if (req.endsWith("\r\n\r\n")) {
            // Methods without body
            User user = Authorizer.authorize(reqComps[2]);

                // body.put("id", Integer.toString(user.id));
            switch (reqComps[0]) {
                case "GET": { res = RouteController.get(url, user != null ? user.id : null); break; }
                case "HEAD": {  res = ResourceController.get(url);
                                writeToKey(key, res.status == 200 ? responseBuffers.createHeaderOnlyBuf(res.json)
                                                                : responseBuffers.get(res.json).duplicate());
                                return;
                             }
                case "OPTIONS": { writeToKey(key, responseBuffers.get("204").duplicate()); return; }
                default: { writeToKey(key, responseBuffers.get("501").duplicate()); return; }
            }
        }

        else {
            Json body = new Json(reqComps[2].substring(reqComps[2].lastIndexOf('\n')+1));
            User user = Authorizer.authorize(reqComps[2]);
            if(user != null)
                body.put("id", Integer.toString(user.id));
            switch (reqComps[0]) {
                // Methods with body
                case "POST": { res = RouteController.post( url, body); break; }
                case "PUT":
                case "PATCH":
                default: { writeToKey(key, responseBuffers.get("501").duplicate()); return; }
            }
        }
        System.out.println("Final res: " + res.status + " " + res.json);
        writeToKey(key, res);
    }


    private static void writeToKey(SelectionKey key, Response res) {
        if(res.status == 200) {
            writeToKey(key, res.json.startsWith("{") || res.json.startsWith("[")
                    ? responseBuffers.createResponseBuffer( FileInfo.json(res.json.getBytes(StandardCharsets.UTF_8)) )
                    : responseBuffers.get(res.json).duplicate() );
            return;
        }
        else if(res.status == 400) {
            writeToKey(key, responseBuffers.createBadRequest(res.json));
            return;
        }

        writeToKey(key, responseBuffers.get(Integer.toString(res.status)).duplicate());
    }

    private static void writeToKey(SelectionKey key, ByteBuffer buffer) {
        key.attach(buffer);
        key.interestOps(SelectionKey.OP_WRITE);
    }


}

package server;

import server.routes.UserController;
import server.services.UserService;
import server.utils.FileInfo;
import server.utils.Parsers;
import server.utils.Response;
import server.utils.Responses;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;

final public class Router {

    public static Responses responseBuffers = new Responses(Server.PUBLIC_HTML_DIR);

    public static void httpRequestHandle(SelectionKey key, String req)
    {
        if (req.endsWith("\r\n\r\n")) {
            String httpMethod = req
                    .codePoints()
                    .takeWhile(c -> c > 32 && c < 127)
                    .collect(StringBuilder::new,
                            StringBuilder::appendCodePoint,
                            StringBuilder::append)
                    .toString()
                    ;
            String url = req.substring(httpMethod.length()+2,
                                       req.indexOf(' ', httpMethod.length()+1)
                                      );



            if(httpMethod.equals("GET"))
                get(key, url);


        }

        else {
            if(req.startsWith("POST"))
                post( key,
                      req.substring(req.indexOf(' ')+1, req.indexOf(' ', 6)),
                      req.substring(req.lastIndexOf('\n')+1)
                );

            else {
                key.attach(responseBuffers.get("501").duplicate());
                key.interestOps(SelectionKey.OP_WRITE);
            }

        }
    }

    public static void get(SelectionKey key, String url)
    {
        System.out.println("Server received request for route: \"" + url + '\"');
        if(url.isEmpty())
            url = "index.html";

        if(FileInfo.isValid(url)) {
            // Get the response buffer
            if (responseBuffers.contains(url))
                key.attach(responseBuffers.get(url).duplicate());
            else
                key.attach(responseBuffers.get("404").duplicate());
        }
        else {
            Response res = UserController.get(url);
            if(res.status == 200)
                key.attach( responseBuffers.createResponseBuffer(
                                FileInfo.json(StandardCharsets.UTF_8,
                                        res.json.getBytes(StandardCharsets.UTF_8))
                        )
                );
            else {
                System.out.println("Failed post: code " + res.status);
                key.attach( responseBuffers.get(Integer.toString(res.status)).duplicate() );
            }
        }

        // Change mode to write - now we will send response to this client
        key.interestOps(SelectionKey.OP_WRITE);
    }

    public static void post(SelectionKey key, String route, String data) {
        System.out.println("Server received request route: \"" + route + '\"');
        System.out.println("Data: \"" + data + '\"');

        Response res = UserController.post(route, data);
        if(res.status == 200) {

            System.out.println("User logged: " + res.json);
            key.attach( responseBuffers.createResponseBuffer(
                                    FileInfo.json(StandardCharsets.UTF_8,
                                    res.json.getBytes(StandardCharsets.UTF_8))
                                    )
                        );
        }
        else {
            System.out.println("Failed post: code " + res.status);
            key.attach( responseBuffers.get(Integer.toString(res.status)).duplicate() );
        }
        key.interestOps(SelectionKey.OP_WRITE);
    }

    /*
    private static String authenticate(String data) {
        Matcher matcher = Parsers.loginPattern.matcher(data);
        String email = null;
        String pass = null;
        if(matcher.find()) {
            email = matcher.group(1);
            // System.out.print("Parsed:  email : " + email);
            pass = matcher.group(2);
            // System.out.print(", pass : " + pass + '\n');
        }
        else {
            return null;
        }
        return UserService.authenticate(email, pass);
    }
     */
}

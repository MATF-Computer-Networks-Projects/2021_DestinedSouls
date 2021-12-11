package server;

import server.services.UserService;
import server.utils.Parsers;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final public class Router {
    public static Response responseBuffers = new Response("Client/dist/client");

    public static void httpRequestHandle(SelectionKey key, String req)
    {
        if (req.endsWith("\r\n\r\n")) {
            // Showing some functional concepts with collect() method,
            // this can be done easily using substring() method
            String httpMethod = req
                    .codePoints()
                    .takeWhile(c -> c > 32 && c < 127)
                    .collect(StringBuilder::new,
                            StringBuilder::appendCodePoint,
                            StringBuilder::append)
                    .toString()
                    ;
            String filename = req.substring(httpMethod.length()+1)
                    .codePoints()
                    .takeWhile(c -> c > 32 && c < 127)
                    .collect(StringBuilder::new,
                            StringBuilder::appendCodePoint,
                            StringBuilder::append)
                    .toString()
                    ;

            if(filename.equals("/"))
                filename = "index.html";
            else if(filename.startsWith("/"))
                filename = filename.substring(1);

            if(httpMethod.equals("GET"))
                get(key, filename);


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

    public static void get(SelectionKey key, String filename)
    {
        System.out.println("Server received request for file: \"" + filename + '\"');
        // Get the response buffer
        if (responseBuffers.contains(filename))
            key.attach(responseBuffers.get(filename).duplicate());
        else
            key.attach(responseBuffers.get("404").duplicate());

        // Change mode to write - now we will send response to this client
        key.interestOps(SelectionKey.OP_WRITE);
    }

    public static void post(SelectionKey key, String route, String data) {
        System.out.println("Server received request route: \"" + route + '\"');
        System.out.println("Data: \"" + data + '\"');


        String res = null;
        switch (route) {
            case "/users/authenticate": {
                res = authenticate(data);
                break;
            }
            case "/users/register": {
                key.attach(responseBuffers.get("501").duplicate());
                key.interestOps(SelectionKey.OP_WRITE);
                return;
            }
            default: {
                key.attach(responseBuffers.get("404").duplicate());
                key.interestOps(SelectionKey.OP_WRITE);
                return;
            }
        }

        if(res == null) {
            key.attach(responseBuffers.get("404").duplicate());
            key.interestOps(SelectionKey.OP_WRITE);
            return;
        }

        if(res == "") {
            key.attach(responseBuffers.get("401").duplicate());
            key.interestOps(SelectionKey.OP_WRITE);
            return;
        }


        res = "{" + res + "}";
        System.out.println("User logged: " + res);

        ByteBuffer buf = responseBuffers.createResponseBuffer(FileInfo.json(StandardCharsets.UTF_8,
                                                              res.getBytes(StandardCharsets.UTF_8)));

        key.attach(buf.duplicate());
        key.interestOps(SelectionKey.OP_WRITE);
    }

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
}

package org.hunters.server;

import org.hunters.server.http.HttpMessageProcessor;
import org.hunters.server.http.HttpMessageReaderFactory;
import org.hunters.server.routes.ResourceController;
import org.hunters.server.routes.Router;
import org.hunters.server.routes.UserController;
import org.hunters.server.services.UserService;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {

        var router = new Router();
        router.addRouteController("/users", new UserController());
        router.addRouteController("/upload", new ResourceController());

        HttpMessageProcessor messageProcessor = new HttpMessageProcessor(router);

        UserService.load();
        Server server = new Server(3000, new HttpMessageReaderFactory(), messageProcessor);

        server.start();
    }

    private static void printBytes(byte[] bytes, int off, int len) {
        for(int i = off; i < off+len; ++i)
            System.out.print((char)bytes[i]);
    }
}

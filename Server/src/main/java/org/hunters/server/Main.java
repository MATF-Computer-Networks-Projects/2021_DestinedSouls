package org.hunters.server;

import org.hunters.server.protocols.http.HttpMessageReaderFactory;
import org.hunters.server.routes.ChatController;
import org.hunters.server.routes.ResourceController;
import org.hunters.server.routes.Router;
import org.hunters.server.routes.UserController;
import org.hunters.server.services.UserService;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {

        var router = new Router();
        router.addRouteController("/users",  new UserController());
        router.addRouteController("/upload", new ResourceController());
        router.addRouteController("/chat",   new ChatController());

        MessageProcessor messageProcessor = new MessageProcessorImpl(router);

        UserService.load();
        Server server = new Server(3000, new HttpMessageReaderFactory(), messageProcessor);

        server.start();
    }
}

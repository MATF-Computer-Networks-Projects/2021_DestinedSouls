package example;

import server.Server;
import server.http.HttpMessageReaderFactory;
import server.routes.Router;
import server.routes.UserController;
import server.services.UserService;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {

        var router = new Router();
        router.addRouteController("/users", new UserController());

        UserService.load();
        Server server = new Server(3000, new HttpMessageReaderFactory(), router);

        server.start();
    }

    private static void printBytes(byte[] bytes, int off, int len) {
        for(int i = off; i < off+len; ++i)
            System.out.print((char)bytes[i]);
    }
}

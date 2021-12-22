package server.routes;

import server.IMessageProcessor;
import server.Message;
import server.WriteProxy;
import server.http.EHttpMethod;
import server.http.HttpHeaders;
import server.services.StorageService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Router implements IMessageProcessor {

    private final Map<String, IController> routes;

    public Router() {
        this.routes = new HashMap<>();
    }

    private static boolean isAllowed(EHttpMethod method) { return method != EHttpMethod.ERROR; }

    public void addRouteController(String route, IController controller) {
        routes.put(route, controller);
    }

    @Override
    public void process(Message request, WriteProxy writeProxy) {
        Message response = writeProxy.getMessage();
        response.socketId = request.socketId;
        HttpHeaders req = (HttpHeaders) request.metaData;

        if(!isAllowed(req.httpMethod)) {
            response.writeToMessage(StorageService.cache.get("405"));
            writeProxy.enqueue(response);
            return;
        }

        System.out.println("Url: " + req.url);

        String filename = req.url.substring(req.url.lastIndexOf('/')+1);
        if(StorageService.cache.contains(filename))
        {
            response.writeToMessage(StorageService.cache.get(filename).duplicate());
            writeProxy.enqueue(response);
            return;
        }

        System.out.println("Url: " + filename);

        int idx = req.url.indexOf('/',1);
        String r = idx == -1 ? req.url : req.url.substring(0, idx);
        if(!routes.containsKey(r)) {
            (new ResourceController()).get(request, response);
            writeProxy.enqueue(response);
            return;
        }


        IController controller = routes.get(r);
        System.out.println("Route: " + controller.toString());

        switch (req.httpMethod) {
            case GET:       { controller.get(request, response);  break; }
            case POST:      { controller.post(request, response); break; }
            case DELETE:    { controller.delete(request, response); break; }
            //case HEAD:
            //case PUT:
            default: {    break; }
        }


        writeProxy.enqueue(response);
    }
}

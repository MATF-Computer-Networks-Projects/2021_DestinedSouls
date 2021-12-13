package server.routes;

import server.IMessageProcessor;
import server.Message;
import server.WriteProxy;
import server.http.EHttpMethod;
import server.http.HttpHeaders;
import server.utils.FileInfo;
import server.utils.Responses;

import java.util.HashMap;
import java.util.Map;

public class Router implements IMessageProcessor {
    static Responses cache = new Responses(FileInfo.PUBLIC_HTML_DIR);

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
            response.writeToMessage(cache.get("405"));
            writeProxy.enqueue(response);
            return;
        }

        if(req.url.equals("/")) {
            response.writeToMessage(cache.get("/"));
            writeProxy.enqueue(response);
            return;
        }

        String filename = req.url.substring(req.url.lastIndexOf('/')+1);
        if(cache.contains(filename))
        {
            response.writeToMessage(Router.cache.get(filename).duplicate());
            writeProxy.enqueue(response);
            return;
        }

        int idx = req.url.indexOf('/',1);
        String r = idx == -1 ? req.url.substring(1) : req.url.substring(0, idx);
        if(!routes.containsKey(r)) {
            //response.writeToMessage(cache.get("404").duplicate());
            //writeProxy.enqueue(response);
            (new ResourceController()).get(request, response);
            writeProxy.enqueue(response);
            return;
        }

        IController controller = routes.get(r);
        System.out.println("Route: " + controller.toString());

        switch (req.httpMethod) {
            case GET:  { controller.get(request, response);  break; }
            case POST: { controller.post(request, response); break; }
            //case HEAD:
            //case PUT:
            //case DELETE: { response.writeToMessage(cache.get("501").duplicate());  break; }
            default: {    break; }
        }


        writeProxy.enqueue(response);
    }
}

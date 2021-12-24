package server.routes;

import server.http.HttpRequest;
import server.utils.Response;

import java.util.HashMap;
import java.util.Map;

public class Router {

    private final Map<String, IController> routes;

    public Router() {
        this.routes = new HashMap<>();
    }

    public void addRouteController(String route, IController controller) {
        routes.put(route, controller);
    }

    public boolean hasRoute(String route) {
        return routes.containsKey(route);
    }

    public Response forward(HttpRequest request) {
        int idx = request.headers.url.indexOf('/',1);
        String route = idx == -1 ? request.headers.url : request.headers.url.substring(0, idx);
        request.headers.url = idx == -1 ? request.headers.url : request.headers.url.substring(route.length());

        if(!routes.containsKey(route) && route.indexOf('.') != -1)
            return ResourceController.get(request.headers.url);

        return routes.get(route).handle(request);
    }
}

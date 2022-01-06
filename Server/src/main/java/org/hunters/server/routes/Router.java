package org.hunters.server.routes;

import org.hunters.server.protocols.http.HttpRequest;
import org.hunters.server.utils.Response;

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

    private String urlResolve(String url) {
        int idx = url.indexOf('/',1);
        return idx == -1 ? url : url.substring(0, idx);
    }

    public Response forward(HttpRequest request) {

        String route = urlResolve(request.headers.url);
        request.headers.url = route.length() == request.headers.url.length() ? request.headers.url
                                                                    : request.headers.url.substring(route.length());

        if(!routes.containsKey(route)) {
            if(route.indexOf('.') != -1)
                return ResourceController.get(request.headers.url);
            else
                return new Response(404);
        }

        return routes.get(route).handle(request);
    }

    public Response forward(String url, String request) {
        String route = urlResolve(url);

        if(!routes.containsKey(route))
            return new Response(404);

        return routes.get(route).handle(request);
    }
}

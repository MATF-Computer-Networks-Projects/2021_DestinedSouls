package server.routes;

import server.utils.Response;

public interface IController {

    Response handle(Object request);
}

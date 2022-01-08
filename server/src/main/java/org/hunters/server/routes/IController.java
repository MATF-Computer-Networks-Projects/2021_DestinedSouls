package org.hunters.server.routes;

import org.hunters.server.utils.Response;

public interface IController {

    Response handle(Object request);
}

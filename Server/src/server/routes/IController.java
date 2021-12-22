package server.routes;

import server.Message;

public interface IController {
    void get(Message request, Message response);
    void post(Message request, Message response);
    void delete(Message request, Message response);
    //void put();
}

package org.hunters.server;


public interface MessageProcessor {

    public void process(Message message, WriteProxy writeProxy);
}

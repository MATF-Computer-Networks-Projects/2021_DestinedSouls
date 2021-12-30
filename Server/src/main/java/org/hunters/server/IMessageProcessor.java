package org.hunters.server;


public interface IMessageProcessor {

    public void process(Message message, WriteProxy writeProxy);
}

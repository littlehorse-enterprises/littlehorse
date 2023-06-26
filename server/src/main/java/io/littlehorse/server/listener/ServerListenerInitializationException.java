package io.littlehorse.server.listener;

public class ServerListenerInitializationException extends RuntimeException {

    public ServerListenerInitializationException(String message) {
        super(message);
    }

    public ServerListenerInitializationException(Throwable cause) {
        super(cause);
    }
}

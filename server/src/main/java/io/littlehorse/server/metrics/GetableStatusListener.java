package io.littlehorse.server.metrics;

public interface GetableStatusListener {
    void listen(GetableStatusUpdate event);
}

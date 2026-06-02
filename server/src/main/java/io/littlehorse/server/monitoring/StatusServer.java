package io.littlehorse.server.monitoring;

import io.littlehorse.server.monitoring.http.ContentType;
import java.io.Closeable;
import java.util.function.Supplier;

public interface StatusServer extends Closeable {

    void start(int port);

    void close();

    void handle(String path, ContentType contentType, Supplier<String> functionResponse);
}

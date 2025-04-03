package io.littlehorse.server;

import io.littlehorse.server.streams.topology.core.RequestExecutionContext;

import java.util.function.Consumer;

public class RequestTask implements Runnable {

    private final RequestExecutionContext requestExecutionContext;
    private final Consumer<RequestExecutionContext> toExecute;

    public RequestTask(RequestExecutionContext requestExecutionContext, Consumer<RequestExecutionContext> toExecute) {
        this.requestExecutionContext = requestExecutionContext;
        this.toExecute = toExecute;
    }

    @Override
    public void run() {
        toExecute.accept(requestExecutionContext);
    }
}

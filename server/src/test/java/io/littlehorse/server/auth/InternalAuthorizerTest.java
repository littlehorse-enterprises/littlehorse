package io.littlehorse.server.auth;

import io.grpc.Context;
import io.littlehorse.server.streams.topology.core.RequestExecutionContext;

public class InternalAuthorizerTest {

    private final Context.Key<RequestExecutionContext> contextKey = Context.key("test-context-key");

    private final InternalAuthorizer internalAuthorizer = new InternalAuthorizer(contextKey);
}

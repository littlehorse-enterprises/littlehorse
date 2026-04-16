package io.littlehorse.sdk.wfsdk.internal;

import io.littlehorse.sdk.common.adapter.LHTypeAdapterRegistry;
import io.littlehorse.sdk.common.proto.PutExternalEventDefRequest;

final class InterruptExternalEventDefRegistration implements ExternalEventDefRegistration {

    private final String externalEventDefName;
    private final Class<?> payloadClass;
    private final LHTypeAdapterRegistry typeAdapterRegistry;

    InterruptExternalEventDefRegistration(
            String externalEventDefName, Class<?> payloadClass, LHTypeAdapterRegistry typeAdapterRegistry) {
        this.externalEventDefName = externalEventDefName;
        this.payloadClass = payloadClass;
        this.typeAdapterRegistry = typeAdapterRegistry;
    }

    @Override
    public String getExternalEventDefName() {
        return externalEventDefName;
    }

    @Override
    public PutExternalEventDefRequest toPutExtDefRequest() {
        return PutExternalEventDefRequest.newBuilder()
                .setName(externalEventDefName)
                .setContentType(BuilderUtil.javaTypeToReturnType(payloadClass, typeAdapterRegistry))
                .build();
    }
}

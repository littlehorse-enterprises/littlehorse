package io.littlehorse.sdk.wfsdk.internal;

import io.littlehorse.sdk.common.proto.PutExternalEventDefRequest;

final class InterruptExternalEventDefRegistration implements ExternalEventDefRegistration {

    private final String externalEventDefName;
    private final Class<?> payloadClass;

    InterruptExternalEventDefRegistration(String externalEventDefName, Class<?> payloadClass) {
        this.externalEventDefName = externalEventDefName;
        this.payloadClass = payloadClass;
    }

    @Override
    public String getExternalEventDefName() {
        return externalEventDefName;
    }

    @Override
    public PutExternalEventDefRequest toPutExtDefRequest() {
        return PutExternalEventDefRequest.newBuilder()
                .setName(externalEventDefName)
                .setContentType(BuilderUtil.javaTypeToReturnType(payloadClass))
                .build();
    }
}

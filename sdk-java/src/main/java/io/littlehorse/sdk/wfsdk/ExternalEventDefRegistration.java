package io.littlehorse.sdk.wfsdk;

import io.littlehorse.sdk.common.proto.PutExternalEventDefRequest;

public interface ExternalEventDefRegistration {
    String getExternalEventDefName();

    PutExternalEventDefRequest toPutExtDefRequest();
}

package io.littlehorse.sdk.wfsdk.internal;

import io.littlehorse.sdk.common.proto.PutExternalEventDefRequest;

/**
 * Internal registration handle for ExternalEventDef creation.
 *
 * <p>Implementations provide the ExternalEventDef name and the corresponding
 * {@link PutExternalEventDefRequest} used by {@link io.littlehorse.sdk.wfsdk.Workflow#getExternalEventDefsToRegister()}.
 */
interface ExternalEventDefRegistration {
    /**
     * @return the ExternalEventDef name to register.
     */
    String getExternalEventDefName();

    /**
     * @return the {@link PutExternalEventDefRequest} used to register this ExternalEventDef.
     */
    PutExternalEventDefRequest toPutExtDefRequest();
}

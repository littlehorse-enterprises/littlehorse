package io.littlehorse.common.model.meta;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.proto.InterruptDefPb;
import io.littlehorse.common.proto.InterruptDefPbOrBuilder;
import io.littlehorse.common.util.LHGlobalMetaStores;

public class InterruptDef extends LHSerializable<InterruptDefPb> {

    public String handlerSpecName;
    public String externalEventDefName;

    @JsonIgnore
    public ThreadSpec ownerThreadSpec;

    @JsonIgnore
    public ThreadSpec handler;

    @JsonIgnore
    public ExternalEventDef eed;

    public Class<InterruptDefPb> getProtoBaseClass() {
        return InterruptDefPb.class;
    }

    public InterruptDefPb.Builder toProto() {
        InterruptDefPb.Builder out = InterruptDefPb
            .newBuilder()
            .setExternalEventDefName(externalEventDefName)
            .setHandlerSpecName(handlerSpecName);
        return out;
    }

    public void initFrom(MessageOrBuilder proto) {
        InterruptDefPbOrBuilder p = (InterruptDefPbOrBuilder) proto;
        handlerSpecName = p.getHandlerSpecName();
        externalEventDefName = p.getExternalEventDefName();
    }

    public static InterruptDef fromProto(InterruptDefPbOrBuilder p) {
        InterruptDef out = new InterruptDef();
        out.initFrom(p);
        return out;
    }

    public void validate(LHGlobalMetaStores client, LHConfig config)
        throws LHValidationError {
        eed = client.getExternalEventDef(externalEventDefName, null);

        if (eed == null) {
            throw new LHValidationError(
                null,
                "Refers to missing ExternalEventDef " + externalEventDefName
            );
        }

        handler = ownerThreadSpec.wfSpec.threadSpecs.get(handlerSpecName);
        if (handler == null) {
            throw new LHValidationError(
                null,
                "Refers to missing ThreadSpec: " + handlerSpecName
            );
        }

        // As of now, Interrupt Handler Threads can only have one input variable.
        // The triggering ExternalEvent gets passed directly to that input variable,
        // which is named as INPUT (a reserved word).

        if (handler.variableDefs.size() > 1) {
            throw new LHValidationError(
                null,
                "Handler thread " + handler.name + " should only have 'INPUT' var."
            );
        } else if (handler.variableDefs.size() == 1) {
            VariableDef theVarDef = handler.variableDefs.get(0);
            if (!theVarDef.name.equals(LHConstants.EXT_EVT_HANDLER_VAR)) {
                throw new LHValidationError(
                    null,
                    "Handler thread " +
                    handler.name +
                    " can only have '" +
                    LHConstants.EXT_EVT_HANDLER_VAR +
                    "' as an input var"
                );
            }
        }
    }
}

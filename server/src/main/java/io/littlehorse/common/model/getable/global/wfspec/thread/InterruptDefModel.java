package io.littlehorse.common.model.getable.global.wfspec.thread;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.validation.InvalidInterruptDefException;
import io.littlehorse.common.model.getable.global.externaleventdef.ExternalEventDefModel;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableDefModel;
import io.littlehorse.common.model.getable.objectId.ExternalEventDefIdModel;
import io.littlehorse.sdk.common.proto.InterruptDef;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.Getter;

@Getter
public class InterruptDefModel extends LHSerializable<InterruptDef> {

    private ExternalEventDefIdModel externalEventDefId;
    public String handlerSpecName;

    public ThreadSpecModel ownerThreadSpecModel;

    public ThreadSpecModel handler;

    public ExternalEventDefModel eed;
    private ExecutionContext context;

    public Class<InterruptDef> getProtoBaseClass() {
        return InterruptDef.class;
    }

    public InterruptDef.Builder toProto() {
        InterruptDef.Builder out = InterruptDef.newBuilder()
                .setExternalEventDefId(externalEventDefId.toProto())
                .setHandlerSpecName(handlerSpecName);
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        InterruptDef p = (InterruptDef) proto;
        handlerSpecName = p.getHandlerSpecName();
        externalEventDefId =
                LHSerializable.fromProto(p.getExternalEventDefId(), ExternalEventDefIdModel.class, context);
        this.context = context;
    }

    public static InterruptDefModel fromProto(InterruptDef p, ExecutionContext context) {
        InterruptDefModel out = new InterruptDefModel();
        out.initFrom(p, context);
        return out;
    }

    public void validate() throws InvalidInterruptDefException {
        eed = context.service().getExternalEventDef(externalEventDefId.getName());

        if (eed == null) {
            throw new InvalidInterruptDefException("Refers to missing ExternalEventDef " + externalEventDefId);
        }

        handler = ownerThreadSpecModel.wfSpec.threadSpecs.get(handlerSpecName);
        if (handler == null) {
            throw new InvalidInterruptDefException("Refers to missing ThreadSpec: " + handlerSpecName);
        }

        // As of now, Interrupt Handler Threads can only have one input variable.
        // The triggering ExternalEvent gets passed directly to that input variable,
        // which is named as INPUT (a reserved word).

        if (handler.variableDefs.size() > 1) {
            throw new InvalidInterruptDefException(
                    "Interrupt Handler thread " + handler.name + " should only have 'INPUT' var.");
        } else if (handler.variableDefs.size() == 1) {
            VariableDefModel theVarDef = handler.getVariableDefs().get(0).getVarDef();
            if (!theVarDef.getName().equals(LHConstants.EXT_EVT_HANDLER_VAR)) {
                throw new InvalidInterruptDefException("Interrupt Handler thread "
                        + handler.name
                        + " can only have '"
                        + LHConstants.EXT_EVT_HANDLER_VAR
                        + "' as an input var");
            }
        }
    }
}

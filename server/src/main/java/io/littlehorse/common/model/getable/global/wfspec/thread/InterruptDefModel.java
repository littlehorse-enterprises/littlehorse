package io.littlehorse.common.model.getable.global.wfspec.thread;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHApiException;
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

    public void validate() throws LHApiException {
        // TODO: refactor DAO to allow passing in the object id.
        eed = context.service().getExternalEventDef(externalEventDefId.getName());

        if (eed == null) {
            throw new LHApiException(
                    Status.INVALID_ARGUMENT, "Refers to missing ExternalEventDef " + externalEventDefId);
        }

        handler = ownerThreadSpecModel.wfSpecModel.threadSpecs.get(handlerSpecName);
        if (handler == null) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "Refers to missing ThreadSpec: " + handlerSpecName);
        }

        // As of now, Interrupt Handler Threads can only have one input variable.
        // The triggering ExternalEvent gets passed directly to that input variable,
        // which is named as INPUT (a reserved word).

        if (handler.variableDefs.size() > 1) {
            throw new LHApiException(
                    Status.INVALID_ARGUMENT, "Handler thread " + handler.name + " should only have 'INPUT' var.");
        } else if (handler.variableDefs.size() == 1) {
            VariableDefModel theVarDef = handler.getVariableDefs().get(0).getVarDef();
            if (!theVarDef.getName().equals(LHConstants.EXT_EVT_HANDLER_VAR)) {
                throw new LHApiException(
                        Status.INVALID_ARGUMENT,
                        "Handler thread "
                                + handler.name
                                + " can only have '"
                                + LHConstants.EXT_EVT_HANDLER_VAR
                                + "' as an input var");
            }
        }
    }
}

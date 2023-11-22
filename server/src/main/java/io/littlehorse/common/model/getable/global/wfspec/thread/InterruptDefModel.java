package io.littlehorse.common.model.getable.global.wfspec.thread;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.dao.ExecutionContext;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.global.externaleventdef.ExternalEventDefModel;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableDefModel;
import io.littlehorse.sdk.common.proto.InterruptDef;

public class InterruptDefModel extends LHSerializable<InterruptDef> {

    public String handlerSpecName;
    public String externalEventDefName;

    public ThreadSpecModel ownerThreadSpecModel;

    public ThreadSpecModel handler;

    public ExternalEventDefModel eed;

    public Class<InterruptDef> getProtoBaseClass() {
        return InterruptDef.class;
    }

    public InterruptDef.Builder toProto() {
        InterruptDef.Builder out = InterruptDef.newBuilder()
                .setExternalEventDefName(externalEventDefName)
                .setHandlerSpecName(handlerSpecName);
        return out;
    }

    @Override
    public void initFrom(Message proto, io.littlehorse.server.streams.topology.core.ExecutionContext context) {
        InterruptDef p = (InterruptDef) proto;
        handlerSpecName = p.getHandlerSpecName();
        externalEventDefName = p.getExternalEventDefName();
    }

    public static InterruptDefModel fromProto(InterruptDef p, io.littlehorse.server.streams.topology.core.ExecutionContext context) {
        InterruptDefModel out = new InterruptDefModel();
        out.initFrom(p, context);
        return out;
    }

    public void validate(ExecutionContext metadataDao, LHServerConfig config) throws LHApiException {
        eed = metadataDao.getExternalEventDef(externalEventDefName);

        if (eed == null) {
            throw new LHApiException(
                    Status.INVALID_ARGUMENT, "Refers to missing ExternalEventDef " + externalEventDefName);
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
            VariableDefModel theVarDef = handler.variableDefs.get(0);
            if (!theVarDef.name.equals(LHConstants.EXT_EVT_HANDLER_VAR)) {
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

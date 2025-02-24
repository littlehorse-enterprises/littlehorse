package io.littlehorse.common.model.getable.core.wfrun.subnoderun;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.LHTimer;
import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.model.corecommand.subcommand.ExternalEventTimeoutModel;
import io.littlehorse.common.model.getable.core.externalevent.ExternalEventModel;
import io.littlehorse.common.model.getable.core.noderun.NodeFailureException;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.core.wfrun.SubNodeRun;
import io.littlehorse.common.model.getable.core.wfrun.failure.FailureModel;
import io.littlehorse.common.model.getable.global.wfspec.node.NodeModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.ExternalEventNodeModel;
import io.littlehorse.common.model.getable.objectId.ExternalEventDefIdModel;
import io.littlehorse.common.model.getable.objectId.ExternalEventIdModel;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.ExternalEventNodeRun;
import io.littlehorse.sdk.common.proto.LHErrorType;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.ProcessorExecutionContext;
import java.util.Date;
import java.util.Optional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class ExternalEventNodeRunModel extends SubNodeRun<ExternalEventNodeRun> {

    private ExternalEventDefIdModel externalEventDefId;
    private Date eventTime;
    private ExternalEventIdModel externalEventId;
    private ExecutionContext executionContext;
    private ProcessorExecutionContext processorContext;
    private boolean timedOut;

    public ExternalEventNodeRunModel() {}

    public ExternalEventNodeRunModel(ExternalEventDefIdModel extEvtId, ProcessorExecutionContext processorContext) {
        this.externalEventDefId = extEvtId;
        this.executionContext = processorContext;
        this.processorContext = processorContext;
    }

    @Override
    public Class<ExternalEventNodeRun> getProtoBaseClass() {
        return ExternalEventNodeRun.class;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        ExternalEventNodeRun p = (ExternalEventNodeRun) proto;
        if (p.hasEventTime()) {
            eventTime = LHUtil.fromProtoTs(p.getEventTime());
        }
        if (p.hasExternalEventId()) {
            externalEventId = LHSerializable.fromProto(p.getExternalEventId(), ExternalEventIdModel.class, context);
        }
        externalEventDefId =
                LHSerializable.fromProto(p.getExternalEventDefId(), ExternalEventDefIdModel.class, context);
        timedOut = p.getTimedOut();
        this.executionContext = context;
    }

    @Override
    public ExternalEventNodeRun.Builder toProto() {
        ExternalEventNodeRun.Builder out = ExternalEventNodeRun.newBuilder()
                .setExternalEventDefId(externalEventDefId.toProto())
                .setTimedOut(timedOut);

        if (eventTime != null) {
            out.setEventTime(LHUtil.fromDate(eventTime));
        }

        if (externalEventId != null) {
            out.setExternalEventId(externalEventId.toProto());
        }

        return out;
    }

    @Override
    public boolean checkIfProcessingCompleted(ProcessorExecutionContext processorContext) throws NodeFailureException {
        if (externalEventId != null) return true;

        if (timedOut) {
            FailureModel failure = new FailureModel("ExternalEvent did not arrive in time", LHErrorType.TIMEOUT.name());
            throw new NodeFailureException(failure);
        }

        NodeModel node = nodeRun.getNode();
        ExternalEventNodeModel eNode = node.getExternalEventNode();

        ExternalEventModel evt = processorContext
                .getableManager()
                .getUnclaimedEvent(nodeRun.getId().getWfRunId(), eNode.getExternalEventDefId());
        if (evt == null) {
            // It hasn't come in yet.
            return false;
        }

        eventTime = evt.getCreatedAt();
        evt.markClaimedBy(nodeRun);

        this.externalEventId = evt.getObjectId();
        return true;
    }

    @Override
    public Optional<VariableValueModel> getOutput(ProcessorExecutionContext processorContext) {
        if (externalEventDefId == null) {
            throw new IllegalStateException("called getOutput() before node finished!");
        }
        return Optional.of(
                processorContext.getableManager().get(externalEventId).getContent());
    }

    /*
     * Need to override this for ExternalEventRun because it's technically in the
     * "RUNNING" status when waiting for the Event, and while waiting it's
     * perfectly fine (in fact, the *most expected*) time for the interrupt to
     * happen.
     */
    @Override
    public boolean maybeHalt(ProcessorExecutionContext processorContext) {
        return true;
    }

    @Override
    public void arrive(Date time, ProcessorExecutionContext processorContext) throws NodeFailureException {
        // Only thing to do is maybe schedule a timeout.
        if (getNode().getExternalEventNode().getTimeoutSeconds() != null) {
            try {
                VariableValueModel timeoutSeconds = nodeRun.getThreadRun()
                        .assignVariable(getNode().externalEventNode.getTimeoutSeconds());
                if (timeoutSeconds.getType() != VariableType.INT) {
                    throw new LHVarSubError(
                            null, "Resulting TimeoutSeconds was of type " + timeoutSeconds.getType() + " not INT!");
                }

                LHTimer timer = new LHTimer();
                timer.key = nodeRun.getPartitionKey().get();
                timer.maturationTime = new Date(new Date().getTime() + (timeoutSeconds.getIntVal() * 1000));

                CommandModel cmd = new CommandModel();
                ExternalEventTimeoutModel timeoutEvt = new ExternalEventTimeoutModel(nodeRun.getId());
                cmd.setSubCommand(timeoutEvt);
                cmd.time = timer.getMaturationTime();

                timer.payload = cmd.toProto().build().toByteArray();
                processorContext.getTaskManager().scheduleTimer(timer);
                log.trace(
                        "Scheduled timeout at {} for external event noderun {}",
                        timer.getMaturationTime(),
                        nodeRun.getId());
            } catch (LHVarSubError exn) {
                throw new NodeFailureException(new FailureModel(
                        "Failed determining timeout for ext evt node: " + exn.getMessage(), LHConstants.VAR_ERROR));
            }
        }
    }

    public void processExternalEventTimeout(ExternalEventTimeoutModel timeout) {
        if (nodeRun.getStatus() == LHStatus.COMPLETED || nodeRun.getStatus() == LHStatus.ERROR) {
            log.trace("ignoring timeout; already completed or failed");
            return;
        }

        timedOut = true;
    }

    public static ExternalEventNodeRunModel fromProto(ExternalEventNodeRun p, ExecutionContext context) {
        ExternalEventNodeRunModel out = new ExternalEventNodeRunModel();
        out.initFrom(p, context);
        return out;
    }
}

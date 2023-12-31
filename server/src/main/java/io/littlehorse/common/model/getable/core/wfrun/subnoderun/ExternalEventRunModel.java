package io.littlehorse.common.model.getable.core.wfrun.subnoderun;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.LHTimer;
import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.model.corecommand.subcommand.ExternalEventTimeoutModel;
import io.littlehorse.common.model.getable.core.externalevent.ExternalEventModel;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.core.wfrun.SubNodeRun;
import io.littlehorse.common.model.getable.core.wfrun.failure.FailureModel;
import io.littlehorse.common.model.getable.global.wfspec.node.NodeModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.ExternalEventNodeModel;
import io.littlehorse.common.model.getable.objectId.ExternalEventDefIdModel;
import io.littlehorse.common.model.getable.objectId.ExternalEventIdModel;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.ExternalEventRun;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.ProcessorExecutionContext;
import java.util.Date;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class ExternalEventRunModel extends SubNodeRun<ExternalEventRun> {

    private ExternalEventDefIdModel externalEventDefId;
    private Date eventTime;
    private ExternalEventIdModel externalEventId;
    private ExecutionContext executionContext;
    private ProcessorExecutionContext processorContext;

    public ExternalEventRunModel() {}

    public ExternalEventRunModel(ExternalEventDefIdModel extEvtId, ProcessorExecutionContext processorContext) {
        this.externalEventDefId = extEvtId;
        this.executionContext = processorContext;
        this.processorContext = processorContext;
    }

    public Class<ExternalEventRun> getProtoBaseClass() {
        return ExternalEventRun.class;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        ExternalEventRun p = (ExternalEventRun) proto;
        if (p.hasEventTime()) {
            eventTime = LHUtil.fromProtoTs(p.getEventTime());
        }
        if (p.hasExternalEventId()) {
            externalEventId = LHSerializable.fromProto(p.getExternalEventId(), ExternalEventIdModel.class, context);
        }
        externalEventDefId =
                LHSerializable.fromProto(p.getExternalEventDefId(), ExternalEventDefIdModel.class, context);
        this.executionContext = context;
        this.processorContext = context.castOnSupport(ProcessorExecutionContext.class);
    }

    public ExternalEventRun.Builder toProto() {
        ExternalEventRun.Builder out =
                ExternalEventRun.newBuilder().setExternalEventDefId(externalEventDefId.toProto());

        if (eventTime != null) {
            out.setEventTime(LHUtil.fromDate(eventTime));
        }

        if (externalEventId != null) {
            out.setExternalEventId(externalEventId.toProto());
        }

        return out;
    }

    public static ExternalEventRunModel fromProto(ExternalEventRun p, ExecutionContext context) {
        ExternalEventRunModel out = new ExternalEventRunModel();
        out.initFrom(p, context);
        return out;
    }

    public void processExternalEventTimeout(ExternalEventTimeoutModel timeout) {
        if (nodeRun.status == LHStatus.COMPLETED || nodeRun.status == LHStatus.ERROR) {
            log.debug("ignoring timeout; already completed or failed");
            return;
        }

        nodeRun.fail(
                new FailureModel("External Event did not arrive in time.", LHConstants.TIMEOUT, null),
                processorContext.currentCommand().getTime());
    }

    public boolean advanceIfPossible(Date time) {
        NodeModel node = nodeRun.getNode();
        ExternalEventNodeModel eNode = node.externalEventNode;

        ExternalEventModel evt = processorContext
                .getableManager()
                .getUnclaimedEvent(nodeRun.getId().getWfRunId(), eNode.getExternalEventDefId());
        if (evt == null) {
            // It hasn't come in yet.
            return false;
        }

        eventTime = evt.getCreatedAt();
        evt.markClaimedBy(nodeRun);

        externalEventId = evt.getObjectId();

        nodeRun.complete(evt.getContent(), time);
        return true;
    }

    /*
     * Need to override this for ExternalEventRun because it's technically in the
     * "RUNNING" status when waiting for the Event, and while waiting it's
     * perfectly fine (in fact, the *most expected*) time for the interrupt to
     * happen.
     */
    @Override
    public boolean canBeInterrupted() {
        return true;
    }

    public void arrive(Date time) {
        // Nothing to do
        nodeRun.status = LHStatus.RUNNING;

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
                nodeRun.fail(
                        new FailureModel(
                                "Failed determining timeout for ext evt node: " + exn.getMessage(),
                                LHConstants.VAR_ERROR),
                        time);
            }
        }
    }
}

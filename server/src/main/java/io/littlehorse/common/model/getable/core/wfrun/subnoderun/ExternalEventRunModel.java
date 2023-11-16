package io.littlehorse.common.model.getable.core.wfrun.subnoderun;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.LHTimer;
import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.model.corecommand.subcommand.ExternalEventTimeout;
import io.littlehorse.common.model.getable.core.externalevent.ExternalEventModel;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.core.wfrun.SubNodeRun;
import io.littlehorse.common.model.getable.core.wfrun.failure.FailureModel;
import io.littlehorse.common.model.getable.global.wfspec.node.NodeModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.ExternalEventNodeModel;
import io.littlehorse.common.model.getable.objectId.ExternalEventIdModel;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.ExternalEventRun;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExternalEventRunModel extends SubNodeRun<ExternalEventRun> {

    public String externalEventDefName;
    public Date eventTime;
    public ExternalEventIdModel externalEventId;
    private ExecutionContext executionContext;

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
        externalEventDefName = p.getExternalEventDefName();
        this.executionContext = context;
    }

    public ExternalEventRun.Builder toProto() {
        ExternalEventRun.Builder out = ExternalEventRun.newBuilder().setExternalEventDefName(externalEventDefName);

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

    public void processExternalEventTimeout(ExternalEventTimeout timeout) {
        if (nodeRunModel.status == LHStatus.COMPLETED || nodeRunModel.status == LHStatus.ERROR) {
            log.debug("ignoring timeout; already completed or failed");
            return;
        }

        nodeRunModel.fail(
                new FailureModel("External Event did not arrive in time.", LHConstants.TIMEOUT, null), timeout.time);
    }

    public boolean advanceIfPossible(Date time) {
        NodeModel node = nodeRunModel.getNode();
        ExternalEventNodeModel eNode = node.externalEventNode;

        ExternalEventModel evt =
                executionContext.wfService().getUnclaimedEvent(nodeRunModel.getWfRunId(), eNode.externalEventDefName);
        if (evt == null) {
            // It hasn't come in yet.
            return false;
        }

        eventTime = evt.getCreatedAt();

        evt.claimed = true;
        evt.nodeRunPosition = nodeRunModel.position;
        evt.threadRunNumber = nodeRunModel.threadRunNumber;

        externalEventId = evt.getObjectId();

        nodeRunModel.complete(evt.content, time);
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
        nodeRunModel.status = LHStatus.RUNNING;

        if (getNode().externalEventNode.timeoutSeconds != null) {
            try {
                VariableValueModel timeoutSeconds =
                        nodeRunModel.getThreadRun().assignVariable(getNode().externalEventNode.timeoutSeconds);
                if (timeoutSeconds.type != VariableType.INT) {
                    throw new LHVarSubError(
                            null, "Resulting TimeoutSeconds was of type " + timeoutSeconds.type + " not INT!");
                }

                LHTimer timer = new LHTimer();
                timer.key = nodeRunModel.wfRunId;
                timer.maturationTime = new Date(new Date().getTime() + (timeoutSeconds.intVal * 1000));

                CommandModel cmd = new CommandModel();
                ExternalEventTimeout timeoutEvt = new ExternalEventTimeout();
                timeoutEvt.time = timer.maturationTime;
                timeoutEvt.nodeRunPosition = nodeRunModel.position;
                timeoutEvt.wfRunId = nodeRunModel.wfRunId;
                timeoutEvt.threadRunNumber = nodeRunModel.threadRunNumber;
                cmd.setSubCommand(timeoutEvt);
                cmd.time = timeoutEvt.time;

                timer.payload = cmd.toProto().build().toByteArray();
                executionContext.getTaskManager().scheduleTimer(timer);
                log.info("Scheduled timer!");
            } catch (LHVarSubError exn) {
                nodeRunModel.fail(
                        new FailureModel(
                                "Failed determining timeout for ext evt node: " + exn.getMessage(),
                                LHConstants.VAR_ERROR),
                        time);
            }
        }
    }
}

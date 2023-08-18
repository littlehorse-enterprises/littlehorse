package io.littlehorse.common.model.wfrun.subnoderun;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.command.Command;
import io.littlehorse.common.model.command.subcommand.ExternalEventTimeout;
import io.littlehorse.common.model.meta.NodeModel;
import io.littlehorse.common.model.meta.subnode.ExternalEventNodeModel;
import io.littlehorse.common.model.objectId.ExternalEventIdModel;
import io.littlehorse.common.model.wfrun.ExternalEventModel;
import io.littlehorse.common.model.wfrun.FailureModel;
import io.littlehorse.common.model.wfrun.LHTimer;
import io.littlehorse.common.model.wfrun.SubNodeRun;
import io.littlehorse.common.model.wfrun.VariableValueModel;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.ExternalEventRun;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.VariableType;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExternalEventRunModel extends SubNodeRun<ExternalEventRun> {

    public String externalEventDefName;
    public Date eventTime;
    public ExternalEventIdModel externalEventId;

    public Class<ExternalEventRun> getProtoBaseClass() {
        return ExternalEventRun.class;
    }

    public void initFrom(Message proto) {
        ExternalEventRun p = (ExternalEventRun) proto;
        if (p.hasEventTime()) {
            eventTime = LHUtil.fromProtoTs(p.getEventTime());
        }
        if (p.hasExternalEventId()) {
            externalEventId =
                    LHSerializable.fromProto(p.getExternalEventId(), ExternalEventIdModel.class);
        }
        externalEventDefName = p.getExternalEventDefName();
    }

    public ExternalEventRun.Builder toProto() {
        ExternalEventRun.Builder out =
                ExternalEventRun.newBuilder().setExternalEventDefName(externalEventDefName);

        if (eventTime != null) {
            out.setEventTime(LHUtil.fromDate(eventTime));
        }

        if (externalEventId != null) {
            out.setExternalEventId(externalEventId.toProto());
        }

        return out;
    }

    public static ExternalEventRunModel fromProto(ExternalEventRun p) {
        ExternalEventRunModel out = new ExternalEventRunModel();
        out.initFrom(p);
        return out;
    }

    public void processExternalEventTimeout(ExternalEventTimeout timeout) {
        if (nodeRunModel.status == LHStatus.COMPLETED || nodeRunModel.status == LHStatus.ERROR) {
            log.debug("ignoring timeout; already completed or failed");
            return;
        }

        nodeRunModel.fail(
                new FailureModel(
                        "External Event did not arrive in time.", LHConstants.TIMEOUT, null),
                timeout.time);
    }

    public boolean advanceIfPossible(Date time) {
        NodeModel node = nodeRunModel.getNode();
        ExternalEventNodeModel eNode = node.externalEventNode;

        ExternalEventModel evt =
                nodeRunModel
                        .getThreadRun()
                        .wfRunModel
                        .getDao()
                        .getUnclaimedEvent(
                                nodeRunModel.getThreadRun().wfRunId, eNode.externalEventDefName);
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
                        nodeRunModel
                                .getThreadRun()
                                .assignVariable(getNode().externalEventNode.timeoutSeconds);
                if (timeoutSeconds.type != VariableType.INT) {
                    throw new LHVarSubError(
                            null,
                            "Resulting TimeoutSeconds was of type "
                                    + timeoutSeconds.type
                                    + " not INT!");
                }

                LHTimer timer = new LHTimer();
                timer.topic = nodeRunModel.getThreadRun().wfRunModel.getDao().getCoreCmdTopic();
                timer.key = nodeRunModel.wfRunId;
                timer.maturationTime =
                        new Date(new Date().getTime() + (timeoutSeconds.intVal * 1000));

                Command cmd = new Command();
                ExternalEventTimeout timeoutEvt = new ExternalEventTimeout();
                timeoutEvt.time = timer.maturationTime;
                timeoutEvt.nodeRunPosition = nodeRunModel.position;
                timeoutEvt.wfRunId = nodeRunModel.wfRunId;
                timeoutEvt.threadRunNumber = nodeRunModel.threadRunNumber;
                cmd.setSubCommand(timeoutEvt);
                cmd.time = timeoutEvt.time;

                timer.payload = cmd.toProto().build().toByteArray();
                nodeRunModel.getThreadRun().wfRunModel.getDao().scheduleTimer(timer);
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

package io.littlehorse.common.model.wfrun.subnoderun;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.command.Command;
import io.littlehorse.common.model.command.subcommand.ExternalEventTimeout;
import io.littlehorse.common.model.meta.Node;
import io.littlehorse.common.model.meta.subnode.ExternalEventNode;
import io.littlehorse.common.model.objectId.ExternalEventId;
import io.littlehorse.common.model.observabilityevent.ObservabilityEvent;
import io.littlehorse.common.model.observabilityevent.events.ExtEvtMatchedOe;
import io.littlehorse.common.model.observabilityevent.events.WaitingForExtEvtOe;
import io.littlehorse.common.model.wfrun.ExternalEvent;
import io.littlehorse.common.model.wfrun.Failure;
import io.littlehorse.common.model.wfrun.LHTimer;
import io.littlehorse.common.model.wfrun.SubNodeRun;
import io.littlehorse.common.model.wfrun.VariableValue;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.jlib.common.proto.ExternalEventRunPb;
import io.littlehorse.jlib.common.proto.LHStatusPb;
import io.littlehorse.jlib.common.proto.TaskResultCodePb;
import io.littlehorse.jlib.common.proto.VariableTypePb;
import java.util.Date;
import org.apache.log4j.Logger;

public class ExternalEventRun extends SubNodeRun<ExternalEventRunPb> {

    private static final Logger log = Logger.getLogger(ExternalEventRun.class);

    public String externalEventDefName;
    public Date eventTime;
    public ExternalEventId externalEventId;

    public Class<ExternalEventRunPb> getProtoBaseClass() {
        return ExternalEventRunPb.class;
    }

    public void initFrom(Message proto) {
        ExternalEventRunPb p = (ExternalEventRunPb) proto;
        if (p.hasEventTime()) {
            eventTime = LHUtil.fromProtoTs(p.getEventTime());
        }
        if (p.hasExternalEventId()) {
            externalEventId =
                LHSerializable.fromProto(
                    p.getExternalEventId(),
                    ExternalEventId.class
                );
        }
        externalEventDefName = p.getExternalEventDefName();
    }

    public ExternalEventRunPb.Builder toProto() {
        ExternalEventRunPb.Builder out = ExternalEventRunPb
            .newBuilder()
            .setExternalEventDefName(externalEventDefName);

        if (eventTime != null) {
            out.setEventTime(LHUtil.fromDate(eventTime));
        }

        if (externalEventId != null) {
            out.setExternalEventId(externalEventId.toProto());
        }

        return out;
    }

    public static ExternalEventRun fromProto(ExternalEventRunPb p) {
        ExternalEventRun out = new ExternalEventRun();
        out.initFrom(p);
        return out;
    }

    public void processExternalEventTimeout(ExternalEventTimeout timeout) {
        if (
            nodeRun.status == LHStatusPb.COMPLETED ||
            nodeRun.status == LHStatusPb.ERROR
        ) {
            log.debug("ignoring timeout; already completed or failed");
            return;
        }

        nodeRun.fail(
            new Failure(
                TaskResultCodePb.TIMEOUT,
                "External Event did not arrive in time.",
                LHConstants.TIMEOUT,
                null
            ),
            timeout.time
        );
    }

    public boolean advanceIfPossible(Date time) {
        Node node = nodeRun.getNode();
        ExternalEventNode eNode = node.externalEventNode;

        ExternalEvent evt = nodeRun.threadRun.wfRun.cmdDao.getUnclaimedEvent(
            nodeRun.threadRun.wfRunId,
            eNode.externalEventDefName
        );
        if (evt == null) {
            // It hasn't come in yet.
            return false;
        }

        eventTime = evt.getCreatedAt();

        evt.claimed = true;
        evt.nodeRunPosition = nodeRun.position;
        evt.threadRunNumber = nodeRun.threadRunNumber;

        externalEventId = evt.getObjectId();

        ExtEvtMatchedOe oe = new ExtEvtMatchedOe();
        oe.extEvtDefName = externalEventDefName;
        oe.extEvtGuid = evt.guid;
        oe.nodeRunPosition = nodeRun.position;
        oe.threadRunNumber = nodeRun.threadRunNumber;
        nodeRun.threadRun.wfRun.cmdDao.addObservabilityEvent(
            new ObservabilityEvent(nodeRun.wfRunId, oe)
        );

        nodeRun.complete(evt.content, time);
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
        nodeRun.status = LHStatusPb.RUNNING;

        if (getNode().externalEventNode.timeoutSeconds != null) {
            try {
                VariableValue timeoutSeconds = nodeRun.threadRun.assignVariable(
                    getNode().externalEventNode.timeoutSeconds
                );
                if (timeoutSeconds.type != VariableTypePb.INT) {
                    throw new LHVarSubError(
                        null,
                        "Resulting TimeoutSeconds was of type " +
                        timeoutSeconds.type +
                        " not INT!"
                    );
                }

                LHTimer timer = new LHTimer();
                timer.topic = nodeRun.threadRun.wfRun.cmdDao.getWfRunEventQueue();
                timer.key = nodeRun.wfRunId;
                timer.maturationTime =
                    new Date(new Date().getTime() + (timeoutSeconds.intVal * 1000));

                Command cmd = new Command();
                ExternalEventTimeout timeoutEvt = new ExternalEventTimeout();
                timeoutEvt.time = timer.maturationTime;
                timeoutEvt.nodeRunPosition = nodeRun.position;
                timeoutEvt.wfRunId = nodeRun.wfRunId;
                timeoutEvt.threadRunNumber = nodeRun.threadRunNumber;
                cmd.setSubCommand(timeoutEvt);
                cmd.time = timeoutEvt.time;

                timer.payload = cmd.toProto().build().toByteArray();
                nodeRun.threadRun.wfRun.cmdDao.scheduleTimer(timer);
                log.info("Scheduled timer!");
            } catch (LHVarSubError exn) {
                nodeRun.fail(
                    new Failure(
                        TaskResultCodePb.VAR_SUB_ERROR,
                        "Failed determining timeout for ext evt node: " +
                        exn.getMessage(),
                        LHConstants.VAR_ERROR
                    ),
                    time
                );
            }
        }

        WaitingForExtEvtOe oe = new WaitingForExtEvtOe();
        oe.extEvtDefName = externalEventDefName;
        oe.nodeRunPosition = nodeRun.position;
        oe.threadRunNumber = nodeRun.threadRunNumber;
        nodeRun.threadRun.wfRun.cmdDao.addObservabilityEvent(
            new ObservabilityEvent(nodeRun.wfRunId, oe)
        );
    }
}

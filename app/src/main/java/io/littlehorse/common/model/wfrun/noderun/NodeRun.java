package io.littlehorse.common.model.wfrun.noderun;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.GETable;
import io.littlehorse.common.model.server.Tag;
import io.littlehorse.common.proto.LHStatusPb;
import io.littlehorse.common.proto.NodeRunPb;
import io.littlehorse.common.proto.NodeRunPb.NodeTypeCase;
import io.littlehorse.common.proto.NodeRunPbOrBuilder;
import io.littlehorse.common.proto.TaskResultCodePb;
import io.littlehorse.common.util.LHUtil;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

public class NodeRun extends GETable<NodeRunPb> {

    public String wfRunId;
    public int threadRunNumber;
    public int position;

    public int number;
    public LHStatusPb status;

    public Date arrivalTime;
    public Date endTime;

    public String wfSpecId;
    public String wfSpecName;
    public String threadSpecName;
    public String nodeName;
    public String taskDefId;

    public TaskResultCodePb resultCode;
    public NodeTypeCase type;

    public TaskRun taskRun;
    public ExternalEventRun externalEventRun;

    public String getObjectId() {
        return NodeRun.getStoreKey(wfRunId, threadRunNumber, position);
    }

    public static String getStoreKey(String wfRunId, int threadNum, int position) {
        return wfRunId + "-" + threadNum + "-" + position;
    }

    public String getPartitionKey() {
        return wfRunId;
    }

    public Class<NodeRunPb> getProtoBaseClass() {
        return NodeRunPb.class;
    }

    public Date getCreatedAt() {
        return arrivalTime;
    }

    public void initFrom(MessageOrBuilder p) {
        NodeRunPbOrBuilder proto = (NodeRunPbOrBuilder) p;
        wfRunId = proto.getWfRunId();
        threadRunNumber = proto.getThreadRunNumber();
        position = proto.getPosition();

        number = proto.getNumber();

        arrivalTime = LHUtil.fromProtoTs(proto.getArrivalTime());
        if (proto.hasEndTime()) {
            endTime = LHUtil.fromProtoTs(proto.getEndTime());
        }

        wfSpecId = proto.getWfSpecId();
        threadSpecName = proto.getThreadSpecName();
        nodeName = proto.getNodeName();
        status = proto.getStatus();

        if (proto.hasResultCode()) resultCode = proto.getResultCode();

        type = proto.getNodeTypeCase();
        switch (type) {
            case TASK:
                taskRun = TaskRun.fromProto(proto.getTask());
                break;
            case EXTERNAL_EVENT:
                externalEventRun =
                    ExternalEventRun.fromProto(proto.getExternalEvent());
            case NODETYPE_NOT_SET:
            default:
                throw new RuntimeException("Not possible");
        }
    }

    public NodeRunPb.Builder toProto() {
        NodeRunPb.Builder out = NodeRunPb
            .newBuilder()
            .setWfRunId(wfRunId)
            .setThreadRunNumber(threadRunNumber)
            .setPosition(position)
            .setNumber(number)
            .setStatus(status)
            .setArrivalTime(LHUtil.fromDate(arrivalTime))
            .setWfSpecId(wfSpecId)
            .setThreadSpecName(threadSpecName)
            .setNodeName(nodeName);

        if (endTime != null) out.setEndTime(LHUtil.fromDate(endTime));

        if (resultCode != null) out.setResultCode(resultCode);

        switch (type) {
            case TASK:
                out.setTask(taskRun.toProto());
                break;
            case EXTERNAL_EVENT:
                out.setExternalEvent(externalEventRun.toProto());
                break;
            case NODETYPE_NOT_SET:
            default:
                throw new RuntimeException("Not possible");
        }

        return out;
    }

    @JsonIgnore
    public List<Tag> getTags() {
        return Arrays.asList(
            new Tag(
                this,
                Pair.of("taskDefId", taskDefId),
                Pair.of("status", status.toString())
            )
        );
    }
}

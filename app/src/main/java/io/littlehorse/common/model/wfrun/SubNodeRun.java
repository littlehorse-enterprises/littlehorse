package io.littlehorse.common.model.wfrun;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.event.WfRunEvent;
import io.littlehorse.common.model.meta.Node;
import io.littlehorse.common.model.meta.WfSpec;
import java.util.Date;

public abstract class SubNodeRun<T extends MessageOrBuilder>
    extends LHSerializable<T> {

    @JsonIgnore
    protected NodeRun nodeRun;

    public abstract void processEvent(WfRunEvent event);

    public abstract void advanceIfPossible(Date time);

    public abstract void arrive(Date time);

    @JsonIgnore
    public void setNodeRun(NodeRun nodeRun) {
        this.nodeRun = nodeRun;
    }

    @JsonIgnore
    public WfSpec getWfSpec() {
        return nodeRun.threadRun.wfRun.wfSpec;
    }

    @JsonIgnore
    public WfRun getWfRun() {
        return nodeRun.threadRun.wfRun;
    }

    @JsonIgnore
    public Node getNode() {
        return nodeRun.getNode();
    }
}

package io.littlehorse.common.model.wfrun;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.event.WfRunEvent;
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
}

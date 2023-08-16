package io.littlehorse.common.model.wfrun;

import com.google.protobuf.Message;
import io.littlehorse.common.LHDAO;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.meta.NodeModel;
import io.littlehorse.common.model.meta.WfSpecModel;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class SubNodeRun<T extends Message> extends LHSerializable<T> {

    public NodeRunModel nodeRunModel;

    public abstract boolean advanceIfPossible(Date time);

    public abstract void arrive(Date time);

    /*
     * The default is that we can't interrupt a node that's making active progress,
     * the clearest example being that when a Task Worker is working on a TaskRun
     * we have to wait for the response to come back before it's safe to initialize
     * an interrupt thread.
     */
    public boolean canBeInterrupted() {
        return !nodeRunModel.isInProgress();
    }

    public void setNodeRunModel(NodeRunModel nodeRunModel) {
        this.nodeRunModel = nodeRunModel;
    }

    public WfSpecModel getWfSpec() {
        return getWfRun().getWfSpecModel();
    }

    public WfRunModel getWfRun() {
        return nodeRunModel.getThreadRun().getWfRunModel();
    }

    public NodeModel getNode() {
        return nodeRunModel.getNode();
    }

    public LHDAO getDao() {
        return nodeRunModel.getThreadRun().getWfRunModel().getDao();
    }
}

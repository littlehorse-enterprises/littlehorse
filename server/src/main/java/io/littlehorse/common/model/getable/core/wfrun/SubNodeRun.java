package io.littlehorse.common.model.getable.core.wfrun;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.CoreObjectId;
import io.littlehorse.common.model.getable.core.noderun.NodeFailureException;
import io.littlehorse.common.model.getable.core.noderun.NodeRunModel;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.node.NodeModel;
import io.littlehorse.common.model.getable.objectId.MetricSpecIdModel;
import io.littlehorse.server.streams.topology.core.ProcessorExecutionContext;
import java.util.Date;
import java.util.Optional;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class SubNodeRun<T extends Message> extends LHSerializable<T> {

    public NodeRunModel nodeRun;

    /**
     * Optionally erform any initial setup/scheduling of stuff when the ThreadRun first arrives at this
     * NodeRun. This is only to be called once.
     * @param time the time at which the NodeRun was arrived at.
     */
    public abstract void arrive(Date time, ProcessorExecutionContext processorContext) throws NodeFailureException;

    /**
     * Returns the output of the NodeRun. Can only be called after completion. Requires the CommandProcessor
     * execution context.
     * @return the output of the SubNodeRun if any output exists.
     */
    public abstract Optional<VariableValueModel> getOutput(ProcessorExecutionContext processorContext);

    /**
     * Checks if the processing of this SubNodeRun has been completed, and returns true. This method can
     * alter the state of the SubNodeRunModel and its dependents but should NOT alter the state of the
     * parent NodeRunModel.
     *
     * Requires the Command Processor execution context.
     * @return true if the processing for this SubNodeRun has been completed; false otherwise.
     * @throws NodeFailureException if the SubNodeRun throws a failure.
     */
    public abstract boolean checkIfProcessingCompleted(ProcessorExecutionContext processorContext)
            throws NodeFailureException;

    /**
     * Maybe halt the SubNodeRun. This is a default implementation which can be overriden by implementations
     * of the SubNodeRun class. The default behavior is to only halt if the NodeRun is not in progress.
     *
     * Default behavior does not modify any state; however, SubNodeRun implementations are free to override
     * this behavior. For example, in the future we may want a HALTED UserTaskRun to change the status of
     * the UserTaskRun from `ASSIGNED` to `ASSIGNED_BUT_HALTED` or something like that...
     * @return true if the SubNodeRun was successfully halted.
     */
    public boolean maybeHalt(ProcessorExecutionContext processorContext) {
        return !nodeRun.isInProgress();
    }

    /**
     * Can be overriden by SubNodeRun implementations to "un-halt" a SubNodeRun. Called by NodeRunModel.
     */
    public void unHalt() {
        // Nothing to do in default case.
    }

    /**
     * Called during initialization. Sets the parent NodeRun.
     * @param nodeRunModel is the NodeRunModel for this subNodeRun.
     */
    public void setNodeRun(NodeRunModel nodeRunModel) {
        this.nodeRun = nodeRunModel;
    }

    /**
     * Returns the WfSpec that this NodeRunModel's NodeRun belongs to. NOTE: during the case
     * of WfSpec Version Migration, we need to check the NodeRun's actual WfSpecId, rather than
     * blindly returning the WfRun's WfSpec, because different NodeRun's in a long-running WfRun
     * can belong to different WfSpec's after a migration has occurred.
     * @return the WfSpecModel for the WfSpec that this NodeRun belongs to.
     */
    public WfSpecModel getWfSpec() {
        return nodeRun.getWfSpec();
    }

    /**
     * Returns the WfRunModel for the WfRun that this SubNodeRun belongs to.
     * @return the WfRunModel for this SubNodeRun
     */
    public WfRunModel getWfRun() {
        return nodeRun.getThreadRun().getWfRun();
    }

    /**
     * Returns the NodeModel for the Node in the WfSpec that this NodeRun represents.
     * @return the Node from the WfSpec.
     */
    public NodeModel getNode() {
        return nodeRun.getNode();
    }

    /**
     * Returns the created sub-Getable for this NodeRunModel. For example, a TaskNodeRunModel would
     * return the associated TaskRunModel.
     */
    public Optional<? extends CoreObjectId<?, ?, ?>> getCreatedSubGetableId() {
        return Optional.empty();
    }

    public Set<MetricSpecIdModel> metricsToCollect() {
        return Set.of();
    }
}

package io.littlehorse.common.model.meta.subnode;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHDAO;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.meta.Node;
import io.littlehorse.common.model.meta.SubNode;
import io.littlehorse.common.model.meta.TaskDef;
import io.littlehorse.common.model.meta.VariableAssignment;
import io.littlehorse.common.model.meta.VariableDef;
import io.littlehorse.common.model.wfrun.ThreadRun;
import io.littlehorse.common.model.wfrun.VarNameAndVal;
import io.littlehorse.common.model.wfrun.VariableValue;
import io.littlehorse.common.model.wfrun.subnoderun.TaskNodeRun;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.sdk.common.proto.TaskNodePb;
import io.littlehorse.sdk.common.proto.VariableAssignmentPb;
import io.littlehorse.sdk.common.proto.VariableTypePb;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskNode extends SubNode<TaskNodePb> {

    public String taskDefName;
    public int retries;
    public List<VariableAssignment> variables;
    public int timeoutSeconds;

    private TaskDef taskDef;
    private LHDAO dao;

    public TaskDef getTaskDef() {
        if (taskDef == null) {
            if (dao == null && node != null) {
                // Only works for when this is part of a Node, not a UTATask.
                dao = node.getThreadSpec().getWfSpec().getDao();
            }
            taskDef = dao.getTaskDef(taskDefName);
        }
        return taskDef;
    }

    private Node node;

    public TaskNode() {
        variables = new ArrayList<>();
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public Class<TaskNodePb> getProtoBaseClass() {
        return TaskNodePb.class;
    }

    public void initFrom(Message proto) {
        TaskNodePb p = (TaskNodePb) proto;
        taskDefName = p.getTaskDefName();
        retries = p.getRetries();

        timeoutSeconds = p.getTimeoutSeconds();
        if (timeoutSeconds == 0) {
            timeoutSeconds = LHConstants.DEFAULT_TASK_TIMEOUT_SECONDS;
        }

        for (VariableAssignmentPb assn : p.getVariablesList()) {
            variables.add(VariableAssignment.fromProto(assn));
        }
    }

    public TaskNodePb.Builder toProto() {
        TaskNodePb.Builder out = TaskNodePb
            .newBuilder()
            .setTaskDefName(taskDefName)
            .setTimeoutSeconds(timeoutSeconds)
            .setRetries(retries);

        for (VariableAssignment va : variables) {
            out.addVariables(va.toProto());
        }
        return out;
    }

    public void validate(LHGlobalMetaStores stores, LHConfig config)
        throws LHValidationError {
        // Want to be able to release new versions of taskdef's and have old
        // workflows automatically use the new version. We will enforce schema
        // compatibility rules on the taskdef to ensure that this isn't an issue.
        TaskDef taskDef = stores.getTaskDef(taskDefName);
        if (taskDef == null) {
            throw new LHValidationError(
                null,
                "Refers to nonexistent TaskDef " + taskDefName
            );
        }
        if (retries < 0) {
            throw new LHValidationError(null, "has negative " + "number of retries!");
        }

        // Now need to validate that all of the variables are provided.
        if (variables.size() != taskDef.inputVars.size()) {
            throw new LHValidationError(
                null,
                "For TaskDef " +
                taskDef.name +
                " we need " +
                taskDef.inputVars.size() +
                " input vars, but we have " +
                variables.size()
            );
        }

        // EMPLOYEE_TODO: do some checking of types so that users can't shoot
        // themselves in the foot with mismatched var types. As part of this large
        // project, we will have to add schemas for JSON typed variables, and do
        // some jsonpath processing as well.

        if (timeoutSeconds == 0) {
            timeoutSeconds = LHConstants.DEFAULT_TASK_TIMEOUT_SECONDS;
        }
    }

    @Override
    public Set<String> getNeededVariableNames() {
        Set<String> out = new HashSet<>();
        for (VariableAssignment assn : variables) {
            out.addAll(assn.getRequiredWfRunVarNames());
        }
        return out;
    }

    public List<VarNameAndVal> assignInputVars(ThreadRun thread)
        throws LHVarSubError {
        List<VarNameAndVal> out = new ArrayList<>();
        if (getTaskDef().getInputVars().size() != variables.size()) {
            throw new LHVarSubError(
                null,
                "Impossible: got different number of taskdef vars and node input vars"
            );
        }

        for (int i = 0; i < taskDef.inputVars.size(); i++) {
            VariableDef requiredVarDef = taskDef.inputVars.get(i);
            VariableAssignment assn = variables.get(i);
            String varName = requiredVarDef.name;
            VariableValue val;

            if (assn != null) {
                val = thread.assignVariable(assn);
            } else {
                throw new LHVarSubError(
                    null,
                    "Variable " + varName + " is unassigned."
                );
            }
            if (val.type != requiredVarDef.type && val.type != VariableTypePb.NULL) {
                throw new LHVarSubError(
                    null,
                    "Variable " +
                    varName +
                    " should be " +
                    requiredVarDef.type +
                    " but is of type " +
                    val.type
                );
            }
            out.add(new VarNameAndVal(varName, val));
        }

        return out;
    }

    @Override
    public TaskNodeRun createSubNodeRun(Date time) {
        TaskNodeRun out = new TaskNodeRun();
        // Note: all of the initialization is done in `TaskNodeRun#arrive()`
        return out;
    }
}

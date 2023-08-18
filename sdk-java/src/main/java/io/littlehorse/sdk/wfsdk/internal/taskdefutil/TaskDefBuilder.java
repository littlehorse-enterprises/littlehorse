package io.littlehorse.sdk.wfsdk.internal.taskdefutil;

import io.littlehorse.sdk.common.exception.TaskSchemaMismatchError;
import io.littlehorse.sdk.common.proto.PutTaskDefRequest;
import io.littlehorse.sdk.common.proto.VariableDef;
import io.littlehorse.sdk.common.proto.VariableType;
import java.util.List;

public class TaskDefBuilder {

    public Object executable;
    public String taskDefName;
    public LHTaskSignature signature;

    public TaskDefBuilder(Object executable, String taskDefName) throws TaskSchemaMismatchError {
        signature = new LHTaskSignature(taskDefName, executable);
        this.executable = executable;
        this.taskDefName = taskDefName;
    }

    public PutTaskDefRequest toPutTaskDefRequest() {
        PutTaskDefRequest.Builder out = PutTaskDefRequest.newBuilder();
        List<String> varNames = signature.getVarNames();
        List<VariableType> varTypes = signature.getParamTypes();

        for (int i = 0; i < varNames.size(); i++) {
            out.addInputVars(
                    VariableDef.newBuilder().setName(varNames.get(i)).setType(varTypes.get(i)));
        }
        out.setName(taskDefName);

        return out.build();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;

        if (!(o instanceof TaskDefBuilder)) return false;
        TaskDefBuilder other = (TaskDefBuilder) o;

        return (signature.equals(other.signature) && taskDefName.equals(other.taskDefName));
    }

    @Override
    public int hashCode() {
        return taskDefName.hashCode();
    }
}

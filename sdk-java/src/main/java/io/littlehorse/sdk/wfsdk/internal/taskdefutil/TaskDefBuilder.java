package io.littlehorse.sdk.wfsdk.internal.taskdefutil;

import io.littlehorse.sdk.common.exception.TaskSchemaMismatchError;
import io.littlehorse.sdk.common.proto.PutTaskDefRequest;
import io.littlehorse.sdk.wfsdk.internal.structdefutil.LHStructDefType;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TaskDefBuilder {

    public Object executable;
    public LHTaskSignature signature;
    public boolean shouldPutStructDefs;
    public List<LHStructDefType> structDefDependencies;

    public TaskDefBuilder(Object executable, String taskDefName, String lhTaskMethodAnnotationValue)
            throws TaskSchemaMismatchError {
        signature = new LHTaskSignature(taskDefName, executable, lhTaskMethodAnnotationValue);
        this.executable = executable;
        this.structDefDependencies = signature.getStructDefDependencies();
    }

    public PutTaskDefRequest toPutTaskDefRequest() {
        PutTaskDefRequest.Builder out = PutTaskDefRequest.newBuilder();

        out.addAllInputVars(signature.getVariableDefs());
        out.setName(this.signature.taskDefName);
        if (signature.getReturnType() != null) {
            out.setReturnType(signature.getReturnType());
        }
        if (signature.getTaskDefDescription() != null) {
            out.setDescription(signature.getTaskDefDescription());
        }

        return out.build();
    }

    public List<LHStructDefType> getStructDefDependencies() {
        return this.structDefDependencies;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;

        if (!(o instanceof TaskDefBuilder)) return false;
        TaskDefBuilder other = (TaskDefBuilder) o;

        return (signature.equals(other.signature)
                && this.signature.getTaskDefName().equals(other.signature.getTaskDefName()));
    }

    @Override
    public int hashCode() {
        return this.signature.getTaskDefName().hashCode();
    }

    public String getTaskDefName() {
        return this.signature.getTaskDefName();
    }
}

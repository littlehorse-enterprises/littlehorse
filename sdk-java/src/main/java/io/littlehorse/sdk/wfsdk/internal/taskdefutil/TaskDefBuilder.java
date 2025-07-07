package io.littlehorse.sdk.wfsdk.internal.taskdefutil;

import io.littlehorse.sdk.common.exception.TaskSchemaMismatchError;
import io.littlehorse.sdk.common.proto.PutTaskDefRequest;
import io.littlehorse.sdk.common.proto.StructDef;
import io.littlehorse.sdk.common.proto.StructDefId;
import io.littlehorse.sdk.wfsdk.internal.structdefutil.StructDefUtil;
import io.littlehorse.sdk.worker.LHStructDef;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TaskDefBuilder {

    public Object executable;
    public LHTaskSignature signature;
    public boolean shouldPutStructDefs;

    public TaskDefBuilder(Object executable, String taskDefName, String lhTaskMethodAnnotationValue)
            throws TaskSchemaMismatchError {
        signature = new LHTaskSignature(taskDefName, executable, lhTaskMethodAnnotationValue);
        this.executable = executable;
    }

    public PutTaskDefRequest toPutTaskDefRequest() {
        PutTaskDefRequest.Builder out = PutTaskDefRequest.newBuilder();

        out.addAllInputVars(signature.getVariableDefs());
        out.setName(this.signature.taskDefName);
        if (signature.getReturnType() != null) {
            out.setReturnType(signature.getReturnType());
        }

        return out.build();
    }

    public List<StructDef> buildStructDefsFromTaskSignature() {
        if (signature.getStructDefDependencies().isEmpty()) return List.of();

        List<StructDef> structDefs = new ArrayList<>();

        for (Class<?> structDefClass : signature.getStructDefDependencies()) {
            LHStructDef lhStructDef = structDefClass.getAnnotation(LHStructDef.class);

            StructDef.Builder structDef = StructDef.newBuilder();
            structDef.setId(StructDefId.newBuilder().setName(lhStructDef.name()));
            structDef.setDescription(lhStructDef.description());
            structDef.setStructDef(StructDefUtil.buildInlineStructDef(structDefClass));

            structDefs.add(structDef.build());
        }

        return structDefs;
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

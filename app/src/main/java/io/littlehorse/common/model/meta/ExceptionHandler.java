package io.littlehorse.common.model.meta;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.proto.ExceptionHandlerPb;
import io.littlehorse.common.proto.ExceptionHandlerPbOrBuilder;
import io.littlehorse.common.proto.VariableMutationPb;
import java.util.ArrayList;
import java.util.List;

public class ExceptionHandler extends LHSerializable<ExceptionHandlerPb> {

    public String specificException;
    public String handlerSpecName;
    public boolean resumeExecution;
    public List<VariableMutation> variableMutations;

    @JsonIgnore
    public Node node;

    public ExceptionHandler() {
        variableMutations = new ArrayList<>();
    }

    public Class<ExceptionHandlerPb> getProtoBaseClass() {
        return ExceptionHandlerPb.class;
    }

    public ExceptionHandlerPb.Builder toProto() {
        ExceptionHandlerPb.Builder out = ExceptionHandlerPb
            .newBuilder()
            .setSpecificException(specificException)
            .setHandlerSpecName(handlerSpecName)
            .setResumeExecution(resumeExecution);

        for (VariableMutation vmut : variableMutations) {
            out.addVariableMutations(vmut.toProto());
        }

        return out;
    }

    public void initFrom(MessageOrBuilder proto) {
        ExceptionHandlerPbOrBuilder p = (ExceptionHandlerPbOrBuilder) proto;
        specificException = p.getSpecificException();
        handlerSpecName = p.getHandlerSpecName();
        resumeExecution = p.getResumeExecution();
        for (VariableMutationPb vpb : p.getVariableMutationsList()) {
            variableMutations.add(VariableMutation.fromProto(vpb));
        }
    }

    public static ExceptionHandler fromProto(ExceptionHandlerPbOrBuilder p) {
        ExceptionHandler out = new ExceptionHandler();
        out.initFrom(p);
        return out;
    }
}

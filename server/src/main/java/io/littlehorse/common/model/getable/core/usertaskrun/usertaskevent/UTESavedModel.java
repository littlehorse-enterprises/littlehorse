package io.littlehorse.common.model.getable.core.usertaskrun.usertaskevent;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.sdk.common.proto.UserTaskEvent.UTESaved;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

@Getter
public class UTESavedModel extends LHSerializable<UTESaved> {

    private String userId;
    private Map<String, VariableValueModel> results;
    private VariableValueModel output;

    public UTESavedModel() {
        this.results = new HashMap<>();
    }

    public UTESavedModel(String userId, Map<String, VariableValueModel> results) {
        this.userId = userId;
        this.results = results;
    }

    public UTESavedModel(String userId, VariableValueModel output) {
        this();
        this.userId = userId;
        this.output = output.getCopy();
    }

    @Override
    public Class<UTESaved> getProtoBaseClass() {
        return UTESaved.class;
    }

    @Override
    public UTESaved.Builder toProto() {
        UTESaved.Builder out = UTESaved.newBuilder();
        out.setUserId(userId);

        for (Map.Entry<String, VariableValueModel> entry : results.entrySet()) {
            out.putResults(entry.getKey(), entry.getValue().toProto().build());
        }
        if (output != null) {
            out.setOutput(output.toProto());
        }

        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext ctx) {
        UTESaved p = (UTESaved) proto;
        userId = p.getUserId();
        output = p.hasOutput() ? VariableValueModel.fromProto(p.getOutput(), ctx) : null;
        for (Map.Entry<String, VariableValue> entry : p.getResultsMap().entrySet()) {
            VariableValueModel varVal = VariableValueModel.fromProto(entry.getValue(), ctx);
            results.put(entry.getKey(), varVal);
        }
    }
}

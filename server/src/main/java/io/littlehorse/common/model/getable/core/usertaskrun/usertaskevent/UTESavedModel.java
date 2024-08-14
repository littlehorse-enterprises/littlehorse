package io.littlehorse.common.model.getable.core.usertaskrun.usertaskevent;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.sdk.common.proto.UserTaskEvent.UTESaved;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UTESavedModel extends LHSerializable<UTESaved> {

    private String userId;
    private Map<String, VariableValueModel> results;

    public UTESavedModel() {
        this.results = new HashMap<>();
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

        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext ctx) {
        UTESaved p = (UTESaved) proto;
        userId = p.getUserId();
        for (Map.Entry<String, VariableValue> entry : p.getResultsMap().entrySet()) {
            VariableValueModel varVal = VariableValueModel.fromProto(entry.getValue(), ctx);
            results.put(entry.getKey(), varVal);
        }
    }
}

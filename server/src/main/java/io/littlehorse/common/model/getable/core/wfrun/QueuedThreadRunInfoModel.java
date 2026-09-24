package io.littlehorse.common.model.getable.core.wfrun;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.sdk.common.proto.QueuedThreadRunInfo;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

@Getter
public class QueuedThreadRunInfoModel extends LHSerializable<QueuedThreadRunInfo> {

    private Map<String, VariableValueModel> inputVars = new HashMap<>();

    public QueuedThreadRunInfoModel() {}

    public QueuedThreadRunInfoModel(Map<String, VariableValueModel> inputVars) {
        this.inputVars = inputVars;
    }

    @Override
    public Class<QueuedThreadRunInfo> getProtoBaseClass() {
        return QueuedThreadRunInfo.class;
    }

    @Override
    public QueuedThreadRunInfo.Builder toProto() {
        QueuedThreadRunInfo.Builder out = QueuedThreadRunInfo.newBuilder();
        for (Map.Entry<String, VariableValueModel> e : inputVars.entrySet()) {
            out.putInputVars(e.getKey(), e.getValue().toProto().build());
        }
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        QueuedThreadRunInfo p = (QueuedThreadRunInfo) proto;
        for (Map.Entry<String, VariableValue> e : p.getInputVarsMap().entrySet()) {
            inputVars.put(e.getKey(), VariableValueModel.fromProto(e.getValue(), context));
        }
    }

    public static QueuedThreadRunInfoModel fromProto(QueuedThreadRunInfo p, ExecutionContext context) {
        QueuedThreadRunInfoModel out = new QueuedThreadRunInfoModel();
        out.initFrom(p, context);
        return out;
    }
}

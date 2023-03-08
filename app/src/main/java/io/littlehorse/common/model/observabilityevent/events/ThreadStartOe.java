package io.littlehorse.common.model.observabilityevent.events;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.LHDAO;
import io.littlehorse.common.model.observabilityevent.SubEvent;
import io.littlehorse.common.model.wfrun.VariableValue;
import io.littlehorse.jlib.common.proto.ThreadStartOePb;
import io.littlehorse.jlib.common.proto.ThreadStartOePbOrBuilder;
import io.littlehorse.jlib.common.proto.ThreadTypePb;
import io.littlehorse.jlib.common.proto.VariableValuePb;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ThreadStartOe extends SubEvent<ThreadStartOePb> {

    public Map<String, VariableValue> variables;
    public int threadRunNumber;
    public String threadSpecName;
    public ThreadTypePb type;

    public ThreadStartOe() {
        variables = new HashMap<>();
    }

    public Class<ThreadStartOePb> getProtoBaseClass() {
        return ThreadStartOePb.class;
    }

    public ThreadStartOePb.Builder toProto() {
        ThreadStartOePb.Builder out = ThreadStartOePb
            .newBuilder()
            .setThreadRunNumber(threadRunNumber)
            .setThreadSpecName(threadSpecName)
            .setType(type);

        for (Map.Entry<String, VariableValue> entry : variables.entrySet()) {
            out.putVariables(entry.getKey(), entry.getValue().toProto().build());
        }

        return out;
    }

    public void initFrom(MessageOrBuilder proto) {
        ThreadStartOePbOrBuilder p = (ThreadStartOePbOrBuilder) proto;
        threadRunNumber = p.getThreadRunNumber();
        threadSpecName = p.getThreadSpecName();
        type = p.getType();

        for (Map.Entry<String, VariableValuePb> entry : p
            .getVariablesMap()
            .entrySet()) {
            variables.put(entry.getKey(), VariableValue.fromProto(entry.getValue()));
        }
    }

    public void updateMetrics(LHDAO dao, Date time, String wfRunId) {
        // Nothingto do
    }
}

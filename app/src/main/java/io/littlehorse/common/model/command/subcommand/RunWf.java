package io.littlehorse.common.model.command.subcommand;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.model.command.SubCommand;
import io.littlehorse.common.model.command.subcommandresponse.RunWfReply;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.common.model.wfrun.VariableValue;
import io.littlehorse.common.proto.LHResponseCodePb;
import io.littlehorse.common.proto.RunWfPb;
import io.littlehorse.common.proto.RunWfPbOrBuilder;
import io.littlehorse.common.proto.VariableValuePb;
import io.littlehorse.server.CommandProcessorDao;
import java.util.HashMap;
import java.util.Map;

public class RunWf extends SubCommand<RunWfPb> {

    public String wfSpecName;
    public Integer wfSpecVersion;
    public Map<String, VariableValue> variables;
    public String id;

    public Class<RunWfPb> getProtoBaseClass() {
        return RunWfPb.class;
    }

    public RunWf() {
        variables = new HashMap<>();
    }

    public RunWfPb.Builder toProto() {
        RunWfPb.Builder out = RunWfPb.newBuilder().setWfSpecName(wfSpecName);
        if (id != null) out.setId(id);
        if (wfSpecVersion != null) out.setWfSpecVersion(wfSpecVersion);

        for (Map.Entry<String, VariableValue> e : variables.entrySet()) {
            out.putVariables(e.getKey(), e.getValue().toProto().build());
        }
        return out;
    }

    public void initFrom(MessageOrBuilder proto) {
        RunWfPbOrBuilder p = (RunWfPbOrBuilder) proto;
        wfSpecName = p.getWfSpecName();
        if (p.hasId()) id = p.getId();
        if (p.hasWfSpecVersion()) wfSpecVersion = p.getWfSpecVersion();

        for (Map.Entry<String, VariableValuePb> e : p.getVariablesMap().entrySet()) {
            variables.put(e.getKey(), VariableValue.fromProto(e.getValue()));
        }
    }

    public boolean hasResponse() {
        return true;
    }

    public RunWfReply process(CommandProcessorDao dao, LHConfig config) {
        RunWfReply out = new RunWfReply();

        WfSpec spec = dao.getWfSpec(wfSpecName, wfSpecVersion);
        if (spec == null) {
            out.code = LHResponseCodePb.NOT_FOUND_ERROR;
            out.message = "Could not find specified WfSpec.";
            return out;
        }

        out.wfSpecVersion = spec.version;

        return out;
    }
}

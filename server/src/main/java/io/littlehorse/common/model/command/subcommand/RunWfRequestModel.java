package io.littlehorse.common.model.command.subcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.dao.CoreProcessorDAO;
import io.littlehorse.common.model.command.SubCommand;
import io.littlehorse.common.model.command.subcommandresponse.RunWfReply;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.LHResponseCode;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.RunWfRequest;
import io.littlehorse.sdk.common.proto.VariableValue;
import java.util.HashMap;
import java.util.Map;

public class RunWfRequestModel extends SubCommand<RunWfRequest> {

    public String wfSpecName;
    public Integer wfSpecVersion;
    public Map<String, VariableValueModel> variables;
    public String id;

    public String getPartitionKey() {
        if (id == null) {
            id = LHUtil.generateGuid();
        }
        return id;
    }

    public Class<RunWfRequest> getProtoBaseClass() {
        return RunWfRequest.class;
    }

    public RunWfRequestModel() {
        variables = new HashMap<>();
    }

    public RunWfRequest.Builder toProto() {
        RunWfRequest.Builder out = RunWfRequest.newBuilder().setWfSpecName(wfSpecName);
        if (id != null) out.setId(id);
        if (wfSpecVersion != null) out.setWfSpecVersion(wfSpecVersion);

        for (Map.Entry<String, VariableValueModel> e : variables.entrySet()) {
            out.putVariables(e.getKey(), e.getValue().toProto().build());
        }
        return out;
    }

    public void initFrom(Message proto) {
        RunWfRequest p = (RunWfRequest) proto;
        wfSpecName = p.getWfSpecName();
        if (p.hasId()) id = p.getId();
        if (p.hasWfSpecVersion()) wfSpecVersion = p.getWfSpecVersion();

        for (Map.Entry<String, VariableValue> e : p.getVariablesMap().entrySet()) {
            variables.put(e.getKey(), VariableValueModel.fromProto(e.getValue()));
        }
    }

    public boolean hasResponse() {
        return true;
    }

    public RunWfReply process(CoreProcessorDAO dao, LHConfig config) {
        RunWfReply out = new RunWfReply();

        WfSpecModel spec = dao.getWfSpec(wfSpecName, wfSpecVersion);
        if (spec == null) {
            out.code = LHResponseCode.NOT_FOUND_ERROR;
            out.message = "Could not find specified WfSpec.";
            return out;
        }
        out.wfSpecVersion = spec.version;

        if (id == null) id = LHUtil.generateGuid();
        out.wfRunId = id;

        WfRunModel oldWfRunModel = dao.getWfRun(id);
        if (oldWfRunModel != null) {
            out.code = LHResponseCode.ALREADY_EXISTS_ERROR;
            out.message = "WfRun with id " + id + " already exists!";
            return out;
        }

        // TODO: Add WfRun Start Metrics

        WfRunModel newRun = spec.startNewRun(this);
        newRun.advance(dao.getEventTime());

        if (newRun.status == LHStatus.ERROR) {
            out.code = LHResponseCode.BAD_REQUEST_ERROR;
            out.message = newRun.threadRunModels.get(0).errorMessage;
        } else {
            out.code = LHResponseCode.OK;
        }
        return out;
    }

    public static RunWfRequestModel fromProto(RunWfRequest p) {
        RunWfRequestModel out = new RunWfRequestModel();
        out.initFrom(p);
        return out;
    }
}

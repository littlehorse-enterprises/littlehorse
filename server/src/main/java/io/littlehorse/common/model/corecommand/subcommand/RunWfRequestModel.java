package io.littlehorse.common.model.corecommand.subcommand;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.corecommand.CoreSubCommand;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.RunWfRequest;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.common.proto.WfRun;
import io.littlehorse.server.streams.storeinternals.GetableManager;
import io.littlehorse.server.streams.storeinternals.ReadOnlyMetadataManager;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.ProcessorExecutionContext;
import java.util.HashMap;
import java.util.Map;
import lombok.Setter;

@Setter
public class RunWfRequestModel extends CoreSubCommand<RunWfRequest> {

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

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        RunWfRequest p = (RunWfRequest) proto;
        wfSpecName = p.getWfSpecName();
        if (p.hasId()) id = p.getId();
        if (p.hasWfSpecVersion()) wfSpecVersion = p.getWfSpecVersion();

        for (Map.Entry<String, VariableValue> e : p.getVariablesMap().entrySet()) {
            variables.put(e.getKey(), VariableValueModel.fromProto(e.getValue(), context));
        }
    }

    public boolean hasResponse() {
        return true;
    }

    @Override
    public WfRun process(ProcessorExecutionContext executionContext, LHServerConfig config) {
        ReadOnlyMetadataManager metadataManager = executionContext.metadataManager();
        GetableManager getableManager = executionContext.getableManager();
        WfSpecModel spec;
        if (wfSpecVersion != null) {
            spec = metadataManager.get(new WfSpecIdModel(wfSpecName, wfSpecVersion));
        } else {
            spec = metadataManager.lastFromPrefix(WfSpecIdModel.getPrefix(wfSpecName));
        }
        if (spec == null) {
            throw new LHApiException(Status.NOT_FOUND, "Couldn't find specified WfSpec");
        }

        if (id == null) {
            id = LHUtil.generateGuid();
        } else {
            WfRunModel oldWfRun = getableManager.get(new WfRunIdModel(id));
            if (oldWfRun != null) {
                throw new LHApiException(Status.ALREADY_EXISTS, "WfRun with id " + id + " already exists!");
            }
        }

        // TODO: Add WfRun Start Metrics

        WfRunModel newRun = spec.startNewRun(this, executionContext);
        newRun.advance(executionContext.currentCommand().getTime());

        return newRun.toProto().build();
    }

    public static RunWfRequestModel fromProto(RunWfRequest p, ExecutionContext context) {
        RunWfRequestModel out = new RunWfRequestModel();
        out.initFrom(p, context);
        return out;
    }
}

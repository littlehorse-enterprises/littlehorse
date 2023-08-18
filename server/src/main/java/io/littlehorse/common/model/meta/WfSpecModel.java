package io.littlehorse.common.model.meta;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.Getable;
import io.littlehorse.common.model.command.subcommand.RunWfRequestModel;
import io.littlehorse.common.model.objectId.WfSpecIdModel;
import io.littlehorse.common.model.wfrun.WfRunModel;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.Node;
import io.littlehorse.sdk.common.proto.ThreadSpec;
import io.littlehorse.sdk.common.proto.ThreadType;
import io.littlehorse.sdk.common.proto.WfSpec;
import io.littlehorse.sdk.common.proto.WfSpecId;
import io.littlehorse.server.streamsimpl.storeinternals.GetableIndex;
import io.littlehorse.server.streamsimpl.storeinternals.IndexedField;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.tuple.Pair;

@Getter
@Setter
public class WfSpecModel extends Getable<WfSpec> {

    public String name;
    public int version;
    public Date createdAt;
    public long lastOffset;
    public int retentionHours;

    public Map<String, ThreadSpecModel> threadSpecs;

    public String entrypointThreadName;
    public LHStatus status;

    private Map<String, String> varToThreadSpec;

    private boolean initializedVarToThreadSpec;

    public WfSpecIdModel getObjectId() {
        return new WfSpecIdModel(name, version);
    }

    public String getName() {
        return name;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getPartitionKey() {
        return LHConstants.META_PARTITION_KEY;
    }

    @Override
    public List<GetableIndex<? extends Getable<?>>> getIndexConfigurations() {
        return List.of(
                new GetableIndex<>(
                        List.of(Pair.of("taskDef", GetableIndex.ValueType.DYNAMIC)),
                        Optional.of(TagStorageType.REMOTE)));
    }

    @Override
    public List<IndexedField> getIndexValues(String key, Optional<TagStorageType> tagStorageType) {
        if (key.equals("taskDef")) {
            return this.taskDefNames().stream()
                    .map(taskDefName -> new IndexedField(key, taskDefName, tagStorageType.get()))
                    .toList();
        }
        return List.of();
    }

    public List<String> taskDefNames() {
        List<String> names = new ArrayList<>();
        threadSpecs.forEach(
                (s, threadSpec) -> {
                    threadSpec
                            .getNodes()
                            .values()
                            .forEach(
                                    node -> {
                                        if (node.getType() == Node.NodeCase.TASK) {
                                            names.add(node.getTaskNode().getTaskDefName());
                                        }
                                    });
                });
        return names;
    }

    public WfSpecModel() {
        threadSpecs = new HashMap<>();
        varToThreadSpec = new HashMap<>();
        initializedVarToThreadSpec = false;
    }

    public void setLastUpdatedOffset(long newOffset) {
        lastOffset = newOffset;
    }

    public WfSpec.Builder toProto() {
        WfSpec.Builder out =
                WfSpec.newBuilder()
                        .setVersion(version)
                        .setCreatedAt(LHUtil.fromDate(createdAt))
                        .setEntrypointThreadName(entrypointThreadName)
                        .setStatus(status)
                        .setRetentionHours(retentionHours)
                        .setName(name);

        if (threadSpecs != null) {
            for (Map.Entry<String, ThreadSpecModel> p : threadSpecs.entrySet()) {
                out.putThreadSpecs(p.getKey(), p.getValue().toProto().build());
            }
        }

        return out;
    }

    public void initFrom(Message pr) {
        WfSpec proto = (WfSpec) pr;
        createdAt = LHUtil.fromProtoTs(proto.getCreatedAt());
        version = proto.getVersion();
        entrypointThreadName = proto.getEntrypointThreadName();
        status = proto.getStatus();
        retentionHours = proto.getRetentionHours();
        name = proto.getName();

        for (Map.Entry<String, ThreadSpec> e : proto.getThreadSpecsMap().entrySet()) {
            ThreadSpecModel ts = new ThreadSpecModel();
            ts.wfSpecModel = this;
            ts.name = e.getKey();
            ts.initFrom(e.getValue());
            threadSpecs.put(e.getKey(), ts);
        }
    }

    public Class<WfSpec> getProtoBaseClass() {
        return WfSpec.class;
    }

    public static WfSpecModel fromProto(WfSpec proto) {
        WfSpecModel out = new WfSpecModel();
        out.initFrom(proto);
        return out;
    }

    private void initializeVarToThreadSpec() {
        initializedVarToThreadSpec = true;
        for (ThreadSpecModel tspec : threadSpecs.values()) {
            for (VariableDefModel vd : tspec.variableDefs) {
                varToThreadSpec.put(vd.name, tspec.name);
            }
        }
    }

    public Pair<String, VariableDefModel> lookupVarDef(String name) {
        if (!initializedVarToThreadSpec) {
            initializeVarToThreadSpec();
        }
        String tspecName = varToThreadSpec.get(name);
        if (tspecName == null) return null;
        VariableDefModel out = threadSpecs.get(tspecName).localGetVarDef(name);
        if (out == null) return null;
        return Pair.of(tspecName, out);
    }

    public void validate(LHGlobalMetaStores dbClient, LHConfig config) throws LHValidationError {
        if (threadSpecs.get(entrypointThreadName) == null) {
            throw new LHValidationError(null, "Unknown entrypoint thread");
        }

        // Validate the variable definitions.
        // This will get tricky with interrupts, but...
        validateVariablesHelper();

        for (Map.Entry<String, ThreadSpecModel> e : threadSpecs.entrySet()) {
            ThreadSpecModel ts = e.getValue();
            try {
                ts.validate(dbClient, config);
            } catch (LHValidationError exn) {
                exn.addPrefix("Thread " + ts.name);
                throw exn;
            }
        }
    }

    // TODO: Do some caching here cuz this could be slow for large workflows.
    public Set<String> getNodeExternalEventDefs() {
        Set<String> out = new HashSet<>();
        for (ThreadSpecModel tspec : threadSpecs.values()) {
            out.addAll(tspec.getNodeExternalEventDefs());
        }
        return out;
    }

    public Map<String, VariableDefModel> getRequiredVariables() {
        return threadSpecs.get(entrypointThreadName).getInputVariableDefs();
    }

    /*
     * For now, the only validation we do for variables is to make sure that:
     * 1. No variable name is defined twice (this will be useful for future features).
     * 2. Every variable name that is referenced by a VariableAssignment or
     *    VariableMutation is defined by *some* thread *somewhere* in the WfSpec.
     *
     * Future work may entail:
     * 1. Validating variable scope across threads (including Exception handlers,
     *    Interrupt Handlers, and child threads).
     * 2. Validating variable types for mutations, assignments, and task input.
     * 3. Incorporation of JsonSchema or Protobuf Schema for further validation.
     */
    private void validateVariablesHelper() throws LHValidationError {
        varToThreadSpec = new HashMap<>();
        for (ThreadSpecModel tspec : threadSpecs.values()) {
            // for (Map.Entry<String, VariableDef> e : tspec.variableDefs.entrySet()) {
            for (VariableDefModel vd : tspec.variableDefs) {
                String varName = vd.name;

                if (varToThreadSpec.containsKey(varName)) {
                    if (!varName.equals(LHConstants.EXT_EVT_HANDLER_VAR)) {
                        throw new LHValidationError(
                                null,
                                "Var name "
                                        + varName
                                        + " defined in threads "
                                        + tspec.name
                                        + " and "
                                        + varToThreadSpec.get(varName));
                    }
                }
                varToThreadSpec.put(varName, name);
            }
        }

        // Seen Vars is now loaded.
        initializeVarToThreadSpec();

        for (ThreadSpecModel tspec : threadSpecs.values()) {
            for (String varName : tspec.getRequiredVariableNames()) {
                if (!varToThreadSpec.containsKey(varName)) {
                    throw new LHValidationError(
                            null, "Thread " + tspec.name + " refers to missing var " + varName);
                }
            }
        }
    }

    public WfRunModel startNewRun(RunWfRequestModel evt) {
        WfRunModel out = new WfRunModel();
        out.setDao(getDao());
        out.id = evt.id;

        out.wfSpecModel = this;
        out.wfSpecVersion = version;
        out.wfSpecName = name;
        out.startTime = getDao().getEventTime();
        out.status = LHStatus.RUNNING;

        out.startThread(
                entrypointThreadName,
                getDao().getEventTime(),
                null,
                evt.variables,
                ThreadType.ENTRYPOINT);

        getDao().saveWfRun(out);

        return out;
    }

    public static WfSpecId parseId(String fullId) {
        String[] split = fullId.split("/");
        return WfSpecId.newBuilder()
                .setName(split[0])
                .setVersion(Integer.valueOf(split[1]))
                .build();
    }
}

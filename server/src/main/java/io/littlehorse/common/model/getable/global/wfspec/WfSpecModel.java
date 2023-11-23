package io.littlehorse.common.model.getable.global.wfspec;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.model.GlobalGetable;
import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.model.corecommand.subcommand.RunWfRequestModel;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableDefModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.Node;
import io.littlehorse.sdk.common.proto.ThreadSpec;
import io.littlehorse.sdk.common.proto.ThreadType;
import io.littlehorse.sdk.common.proto.WfSpec;
import io.littlehorse.sdk.common.proto.WfSpecId;
import io.littlehorse.server.streams.storeinternals.GetableIndex;
import io.littlehorse.server.streams.storeinternals.GetableManager;
import io.littlehorse.server.streams.storeinternals.index.IndexedField;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.ProcessorExecutionContext;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.tuple.Pair;

@Getter
@Setter
public class WfSpecModel extends GlobalGetable<WfSpec> {

    public String name;
    public int version;
    public Date createdAt;
    public long lastOffset;
    private WorkflowRetentionPolicyModel retentionPolicy;
    private WfSpecVersionMigrationModel migration;
    public Map<String, ThreadSpecModel> threadSpecs;

    public String entrypointThreadName;
    public LHStatus status;

    // Internal, not related to Proto.
    private Map<String, String> varToThreadSpec;

    private boolean initializedVarToThreadSpec;
    private ExecutionContext executionContext;

    public WfSpecIdModel getObjectId() {
        return new WfSpecIdModel(name, version);
    }

    public String getName() {
        return name;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    @Override
    public List<GetableIndex<? extends AbstractGetable<?>>> getIndexConfigurations() {
        return List.of(new GetableIndex<>(
                List.of(Pair.of("taskDef", GetableIndex.ValueType.DYNAMIC)), Optional.of(TagStorageType.REMOTE)));
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
        threadSpecs.forEach((s, threadSpec) -> {
            threadSpec.getNodes().values().forEach(node -> {
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

    @Override
    public WfSpec.Builder toProto() {
        WfSpec.Builder out = WfSpec.newBuilder()
                .setVersion(version)
                .setCreatedAt(LHUtil.fromDate(createdAt))
                .setEntrypointThreadName(entrypointThreadName)
                .setStatus(status)
                .setName(name);

        if (threadSpecs != null) {
            for (Map.Entry<String, ThreadSpecModel> p : threadSpecs.entrySet()) {
                out.putThreadSpecs(p.getKey(), p.getValue().toProto().build());
            }
        }

        if (migration != null) {
            out.setMigration(migration.toProto());
        }

        if (retentionPolicy != null) {
            out.setRetentionPolicy(retentionPolicy.toProto());
        }

        return out;
    }

    @Override
    public void initFrom(Message pr, ExecutionContext context) {
        WfSpec proto = (WfSpec) pr;
        createdAt = LHUtil.fromProtoTs(proto.getCreatedAt());
        version = proto.getVersion();
        entrypointThreadName = proto.getEntrypointThreadName();
        status = proto.getStatus();
        name = proto.getName();

        for (Map.Entry<String, ThreadSpec> e : proto.getThreadSpecsMap().entrySet()) {
            ThreadSpecModel ts = new ThreadSpecModel();
            ts.wfSpecModel = this;
            ts.name = e.getKey();
            ts.initFrom(e.getValue(), context);
            threadSpecs.put(e.getKey(), ts);
        }

        if (proto.hasMigration()) {
            migration = LHSerializable.fromProto(proto.getMigration(), WfSpecVersionMigrationModel.class, context);
        }

        if (proto.hasRetentionPolicy()) {
            retentionPolicy =
                    LHSerializable.fromProto(proto.getRetentionPolicy(), WorkflowRetentionPolicyModel.class, context);
        }
        this.executionContext = context;
    }

    public Class<WfSpec> getProtoBaseClass() {
        return WfSpec.class;
    }

    public static WfSpecModel fromProto(WfSpec proto, ExecutionContext context) {
        WfSpecModel out = new WfSpecModel();
        out.initFrom(proto, context);
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

    public void validate(WfSpecModel oldVersion) throws LHApiException {
        if (threadSpecs.get(entrypointThreadName) == null) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "Unknown entrypoint thread");
        }

        if (oldVersion != null) {
            validatePersistentVariables(oldVersion);
        }

        // Validate the variable definitions.
        // This will get tricky with interrupts, but...
        validateVariablesHelper();

        for (Map.Entry<String, ThreadSpecModel> e : threadSpecs.entrySet()) {
            ThreadSpecModel ts = e.getValue();
            try {
                ts.validate();
            } catch (LHApiException exn) {
                throw exn.getCopyWithPrefix("Thread " + ts.name);
            }
        }
    }

    private void validatePersistentVariables(WfSpecModel old) throws LHApiException {
        Set<VariableDefModel> oldPersistentVars = old.getPersistentVariables();
        for (VariableDefModel oldVar : oldPersistentVars) {
            validateCompatibilityWith(oldVar);
        }
    }

    private Set<VariableDefModel> getAllVariables() {
        return threadSpecs.values().stream()
                .flatMap(tSpec -> tSpec.variableDefs.stream())
                .collect(Collectors.toSet());
    }

    private Set<VariableDefModel> getPersistentVariables() {
        return getAllVariables().stream()
                .filter(variable -> variable.isPersistent())
                .collect(Collectors.toSet());
    }

    private void validateCompatibilityWith(VariableDefModel oldVar) throws LHApiException {
        Optional<VariableDefModel> current = getAllVariables().stream()
                .filter(candidate -> candidate.getName().equals(oldVar.getName()))
                .findFirst();
        if (current.isEmpty() || !current.get().hasIndex() || !current.get().isCompatibleWith(oldVar)) {
            throw new LHApiException(
                    Status.INVALID_ARGUMENT,
                    "Must provide variable " + oldVar.getName() + " of type " + oldVar.getType()
                            + ". See the previous version of WfSpec for details.");
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
     * 1. No variable name is defined twice (this will be useful for future
     * features).
     * 2. Every variable name that is referenced by a VariableAssignment or
     * VariableMutation is defined by *some* thread *somewhere* in the WfSpec.
     *
     * Future work may entail:
     * 1. Validating variable scope across threads (including Exception handlers,
     * Interrupt Handlers, and child threads).
     * 2. Validating variable types for mutations, assignments, and task input.
     * 3. Incorporation of JsonSchema or Protobuf Schema for further validation.
     */
    private void validateVariablesHelper() throws LHApiException {
        varToThreadSpec = new HashMap<>();
        for (ThreadSpecModel tspec : threadSpecs.values()) {
            // for (Map.Entry<String, VariableDef> e : tspec.variableDefs.entrySet()) {
            for (VariableDefModel vd : tspec.variableDefs) {
                String varName = vd.name;

                if (varToThreadSpec.containsKey(varName)) {
                    if (!varName.equals(LHConstants.EXT_EVT_HANDLER_VAR)) {
                        throw new LHApiException(
                                Status.INVALID_ARGUMENT,
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
                    throw new LHApiException(
                            Status.INVALID_ARGUMENT, "Thread " + tspec.name + " refers to missing var " + varName);
                }
            }
        }
    }

    public WfRunModel startNewRun(RunWfRequestModel evt) {
        ProcessorExecutionContext processorExecutionContext =
                executionContext.castOnSupport(ProcessorExecutionContext.class);
        CommandModel currentCommand = processorExecutionContext.currentCommand();
        GetableManager getableManager = processorExecutionContext.getableManager();
        WfRunModel out = new WfRunModel();
        out.id = evt.id;

        out.setWfSpec(this);
        out.wfSpecVersion = version;
        out.wfSpecName = name;
        out.startTime = currentCommand.getTime();
        out.status = LHStatus.RUNNING;

        out.startThread(entrypointThreadName, currentCommand.getTime(), null, evt.variables, ThreadType.ENTRYPOINT);
        getableManager.put(out);
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

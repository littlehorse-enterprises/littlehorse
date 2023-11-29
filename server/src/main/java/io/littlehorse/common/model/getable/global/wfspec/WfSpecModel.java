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
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadVarDefModel;
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
import io.littlehorse.server.streams.topology.core.MetadataCommandExecution;
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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

@Getter
@Setter
@Slf4j
public class WfSpecModel extends GlobalGetable<WfSpec> {

    public String name;
    public int version;
    public Date createdAt;
    public long lastOffset;
    private WorkflowRetentionPolicyModel retentionPolicy;
    private WfSpecVersionMigrationModel migration;
    public Map<String, ThreadSpecModel> threadSpecs = new HashMap<>();

    public String entrypointThreadName;

    // Internal, not related to Proto.
    private Map<String, String> varToThreadSpec = new HashMap<>();

    private boolean initializedVarToThreadSpec = false;
    private ExecutionContext executionContext;

    public WfSpecModel() {
        // default constructor used by LHDeserializers
    }

    public WfSpecModel(MetadataCommandExecution executionContext) {
        this.executionContext = executionContext;
    }

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
                List.of(Pair.of("taskDef", GetableIndex.ValueType.DYNAMIC)), Optional.of(TagStorageType.LOCAL)));
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

    public void setLastUpdatedOffset(long newOffset) {
        lastOffset = newOffset;
    }

    @Override
    public WfSpec.Builder toProto() {
        WfSpec.Builder out = WfSpec.newBuilder()
                .setVersion(version)
                .setCreatedAt(LHUtil.fromDate(createdAt))
                .setEntrypointThreadName(entrypointThreadName)
                .setName(name);

        if (threadSpecs != null) {
            for (Map.Entry<String, ThreadSpecModel> p : threadSpecs.entrySet()) {
                out.putThreadSpecs(p.getKey(), p.getValue().toProto().build());
            }
        }

        if (retentionPolicy != null) {
            out.setRetentionPolicy(retentionPolicy.toProto());
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
        name = proto.getName();

        for (Map.Entry<String, ThreadSpec> e : proto.getThreadSpecsMap().entrySet()) {
            ThreadSpecModel ts = new ThreadSpecModel();
            ts.wfSpecModel = this;
            ts.name = e.getKey();
            ts.initFrom(e.getValue(), context);
            threadSpecs.put(e.getKey(), ts);
        }

        if (proto.hasRetentionPolicy()) {
            retentionPolicy =
                    LHSerializable.fromProto(proto.getRetentionPolicy(), WorkflowRetentionPolicyModel.class, context);
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
            for (ThreadVarDefModel vd : tspec.variableDefs) {
                varToThreadSpec.put(vd.getVarDef().getName(), tspec.name);
            }
        }
    }

    public Pair<String, ThreadVarDefModel> lookupVarDef(String name) {
        if (!initializedVarToThreadSpec) {
            initializeVarToThreadSpec();
        }
        String tspecName = varToThreadSpec.get(name);
        if (tspecName == null) return null;
        ThreadVarDefModel out = threadSpecs.get(tspecName).localGetVarDef(name);
        if (out == null) return null;
        return Pair.of(tspecName, out);
    }

    public void validate(WfSpecModel oldVersion) throws LHApiException {
        if (threadSpecs.get(entrypointThreadName) == null) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "Unknown entrypoint thread");
        }

        if (oldVersion != null) {
            log.warn("UNIMPLEMENTED: Enforce WfSpec Compatibility Rules");
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

    /**
     * Returns a Map from Variable Name to ThreadVarDefModel. Excludes the reserved "INPUT"
     * variable.
     * @return a mapping from variable name to ThreadVarDefModel for all variables in the
     * WfSpec.
     */
    public Map<String, ThreadVarDefModel> getAllVariables() {
        return threadSpecs.values().stream()
                .flatMap(tSpec -> tSpec.variableDefs.stream())
                .filter(threadVarDef -> !threadVarDef.getVarDef().getName().equals(LHConstants.EXT_EVT_HANDLER_VAR))
                .collect(Collectors.toMap(
                        threadVarDef -> threadVarDef.getVarDef().getName(), threadVarDef -> threadVarDef));
    }

    // TODO: Do some caching here cuz this could be slow for large workflows.
    public Set<String> getNodeExternalEventDefs() {
        Set<String> out = new HashSet<>();
        for (ThreadSpecModel tspec : threadSpecs.values()) {
            out.addAll(tspec.getNodeExternalEventDefs());
        }
        return out;
    }

    public Map<String, ThreadVarDefModel> getRequiredVariables() {
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
            for (ThreadVarDefModel tvd : tspec.getVariableDefs()) {
                VariableDefModel vd = tvd.getVarDef();
                String varName = vd.getName();

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

    /*
    1. direct pass method argument
    2. setDAO :(
    3. implicitly pass context
     */
    public WfRunModel startNewRun(RunWfRequestModel evt, ProcessorExecutionContext processorContext) {
        ProcessorExecutionContext processorExecutionContext =
                executionContext.castOnSupport(ProcessorExecutionContext.class);
        CommandModel currentCommand = processorExecutionContext.currentCommand();
        GetableManager getableManager = processorExecutionContext.getableManager();
        WfRunModel out = new WfRunModel(processorExecutionContext);
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

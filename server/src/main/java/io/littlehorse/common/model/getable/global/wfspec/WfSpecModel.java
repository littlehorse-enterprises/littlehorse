package io.littlehorse.common.model.getable.global.wfspec;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.model.MetadataGetable;
import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.model.corecommand.subcommand.RunWfRequestModel;
import io.littlehorse.common.model.getable.ObjectIdModel;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadVarDefModel;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableDefModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.common.util.WfSpecUtil;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.Node;
import io.littlehorse.sdk.common.proto.TaskNode.TaskToExecuteCase;
import io.littlehorse.sdk.common.proto.ThreadSpec;
import io.littlehorse.sdk.common.proto.ThreadType;
import io.littlehorse.sdk.common.proto.ThreadVarDef;
import io.littlehorse.sdk.common.proto.WfRunVariableAccessLevel;
import io.littlehorse.sdk.common.proto.WfSpec;
import io.littlehorse.sdk.common.proto.WfSpecId;
import io.littlehorse.server.streams.storeinternals.GetableIndex;
import io.littlehorse.server.streams.storeinternals.GetableManager;
import io.littlehorse.server.streams.storeinternals.index.IndexedField;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.MetadataCommandExecution;
import io.littlehorse.server.streams.topology.core.ProcessorExecutionContext;
import java.util.ArrayList;
import java.util.Collection;
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
public class WfSpecModel extends MetadataGetable<WfSpec> {

    private WfSpecIdModel id = new WfSpecIdModel();

    public Date createdAt;
    public long lastOffset;
    private WorkflowRetentionPolicyModel retentionPolicy;

    public Map<String, ThreadSpecModel> threadSpecs = new HashMap<>();
    private Map<String, ThreadVarDefModel> frozenVariables = new HashMap<>();

    public String entrypointThreadName;
    private WfSpecVersionMigrationModel migration;
    private ParentWfSpecReferenceModel parentWfSpec;

    // Internal, not related to Proto.
    private Map<String, String> varToThreadSpec = new HashMap<>();
    private boolean initializedVarToThreadSpec = false;
    private MetadataCommandExecution executionContext;

    public WfSpecModel() {
        // default constructor used by LHDeserializers
    }

    public WfSpecModel(MetadataCommandExecution executionContext) {
        this.executionContext = executionContext;
    }

    public WfSpecIdModel getObjectId() {
        return id;
    }

    public String getName() {
        return id.getName();
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
                if (node.getType() == Node.NodeCase.TASK
                        && node.getTaskNode().getTaskToExecuteType() == TaskToExecuteCase.TASK_DEF_ID) {
                    names.add(node.getTaskNode().getTaskDefId().getName());
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
                .setId(id.toProto())
                .setCreatedAt(LHUtil.fromDate(createdAt))
                .setEntrypointThreadName(entrypointThreadName);

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

        for (ThreadVarDefModel tvdm : frozenVariables.values()) {
            out.addFrozenVariables(tvdm.toProto());
        }
        if (parentWfSpec != null) out.setParentWfSpec(parentWfSpec.toProto());

        return out;
    }

    @Override
    public void initFrom(Message pr, ExecutionContext context) {
        WfSpec proto = (WfSpec) pr;
        createdAt = LHUtil.fromProtoTs(proto.getCreatedAt());
        entrypointThreadName = proto.getEntrypointThreadName();
        id = LHSerializable.fromProto(proto.getId(), WfSpecIdModel.class, context);

        for (Map.Entry<String, ThreadSpec> e : proto.getThreadSpecsMap().entrySet()) {
            ThreadSpecModel ts = new ThreadSpecModel();
            ts.wfSpec = this;
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

        for (ThreadVarDef tvd : proto.getFrozenVariablesList()) {
            ThreadVarDefModel tvdm = LHSerializable.fromProto(tvd, ThreadVarDefModel.class, context);
            frozenVariables.put(tvdm.getVarDef().getName(), tvdm);
        }

        if (proto.hasParentWfSpec()) {
            parentWfSpec = LHSerializable.fromProto(
                    proto.getParentWfSpec(), ParentWfSpecReferenceModel.class, executionContext);
        }
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

    public void validateAndMaybeBumpVersion(Optional<WfSpecModel> oldVersion, MetadataCommandExecution ctx)
            throws LHApiException {
        if (threadSpecs.get(entrypointThreadName) == null) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "Unknown entrypoint thread");
        }

        validateVariablesHelper(ctx);

        for (Map.Entry<String, ThreadSpecModel> e : threadSpecs.entrySet()) {
            ThreadSpecModel ts = e.getValue();
            try {
                ts.validate(ctx);
            } catch (LHApiException exn) {
                throw exn.getCopyWithPrefix("Thread " + ts.name);
            }
        }

        if (oldVersion.isPresent()) {
            checkCompatibilityAndSetVersion(oldVersion.get());
        }

        if (parentWfSpec != null) {
            getParentWfSpec(ctx);
        }
    }

    /**
     * Returns entrypoint thread instance
     */
    public ThreadSpecModel getEntrypointThread() {
        return threadSpecs.get(entrypointThreadName);
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

    public Map<String, ThreadVarDefModel> getSearchableVariables() {
        Map<String, ThreadVarDefModel> out = new HashMap<>();
        for (ThreadSpecModel thread : threadSpecs.values()) {
            for (ThreadVarDefModel tvdm : thread.getSearchableVarDefs()) {
                out.put(tvdm.getVarDef().getName(), tvdm);
            }
        }
        return out;
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
    private void validateVariablesHelper(MetadataCommandExecution ctx) throws LHApiException {
        varToThreadSpec = new HashMap<>();
        boolean hasParentWorkflow = parentWfSpec != null;
        WfSpecModel parentWfSpec = null;
        if (hasParentWorkflow) {
            parentWfSpec = getParentWfSpec(ctx);
        }
        for (ThreadSpecModel tspec : threadSpecs.values()) {
            // for (Map.Entry<String, VariableDef> e : tspec.variableDefs.entrySet()) {
            for (ThreadVarDefModel tvd : tspec.getVariableDefs()) {
                VariableDefModel vd = tvd.getVarDef();
                String varName = vd.getName();
                if (tvd.getAccessLevel().equals(WfRunVariableAccessLevel.INHERITED_VAR) && !hasParentWorkflow) {
                    throw new LHApiException(
                            Status.INVALID_ARGUMENT, "Only child workflows are allowed to access inherited variables");
                } else if (tvd.getAccessLevel().equals(WfRunVariableAccessLevel.INHERITED_VAR)) {
                    ThreadVarDefModel parentVariable =
                            parentWfSpec.getAllVariables().get(varName);
                    if (parentVariable == null) {
                        throw new LHApiException(
                                Status.INVALID_ARGUMENT,
                                "Inherited variable %s does not exist in parent WfSpec".formatted(varName));
                    } else if (parentVariable.getAccessLevel().equals(WfRunVariableAccessLevel.PRIVATE_VAR)) {
                        throw new LHApiException(
                                Status.INVALID_ARGUMENT,
                                "Inherited variable %s is defined as PRIVATE in parent WfSpec".formatted(varName));
                    }
                }
                if (varToThreadSpec.containsKey(varName)) {
                    if (!varName.equals(LHConstants.EXT_EVT_HANDLER_VAR)) {
                        throw new LHApiException(
                                Status.INVALID_ARGUMENT,
                                "Var name %s defined in threads %s and %s"
                                        .formatted(varName, tspec.getName(), varToThreadSpec.get(varName)));
                    }
                }
                varToThreadSpec.put(varName, tspec.getName());
            }
        }
        // Seen Vars is now loaded.
        initializedVarToThreadSpec = true;

        for (ThreadSpecModel tspec : threadSpecs.values()) {
            for (String varName : tspec.getNamesOfVariablesUsed()) {
                if (!varToThreadSpec.containsKey(varName)) {
                    throw new LHApiException(
                            Status.INVALID_ARGUMENT, "Thread " + tspec.name + " refers to missing var " + varName);
                }
            }
        }

        // Now we curate the list of variables which are "frozen" in time and cannot
        // change their types. This includes two types:
        // - Required variables in the entrypoint threadRun
        // - Any variable with the access_level `PUBLIC_VAR`.
        for (ThreadVarDefModel tvd : getEntrypointThread().getRequiredVarDefs()) {
            frozenVariables.put(tvd.getVarDef().getName(), tvd);
        }
        for (ThreadSpecModel thread : threadSpecs.values()) {
            for (ThreadVarDefModel tvd : thread.getPublicVarDefs()) {
                frozenVariables.put(tvd.getVarDef().getName(), tvd);
            }
        }
    }

    /**
     * Returns a ThreadVarDef for every PUBLIC_VAR variable in the WfSpec (all threads).
     */
    public Collection<ThreadVarDefModel> getPublicVars() {
        return threadSpecs.values().stream()
                .flatMap(tspec -> tspec.getPublicVarDefs().stream())
                .toList();
    }

    private void checkCompatibilityAndSetVersion(WfSpecModel old) {
        // First, for every previously-frozen variable, we need to check that either:
        // - the variable isn't included, or
        // - the variable has the same type.
        for (Map.Entry<String, ThreadVarDefModel> frozenVarDef :
                old.getFrozenVariables().entrySet()) {
            String varName = frozenVarDef.getKey();
            ThreadVarDefModel oldDef = frozenVarDef.getValue();
            ThreadVarDefModel currentVarDef = getAllVariables().get(varName);

            if (currentVarDef != null) {
                // We check that the current one is compatible with the old.
                // TODO: validate jsonpath stuff.
                if (!oldDef.getVarDef().getType().equals(currentVarDef.getVarDef().getType())) {
                    throw new LHApiException(
                            Status.FAILED_PRECONDITION,
                            "Variable %s must be of type %s not %s as it was formerly declared a PUBLIC_VAR"
                                    .formatted(
                                            varName,
                                            oldDef.getVarDef().getType(),
                                            currentVarDef.getVarDef().getType()));
                }
            } else {
                // We need to propagate the information forwards.
                frozenVariables.put(varName, oldDef);
            }
        }

        if (WfSpecUtil.hasBreakingChanges(this, old)) {
            id.setMajorVersion(old.getId().getMajorVersion() + 1);
            id.setRevision(0);
        } else {
            id.setMajorVersion(old.getId().getMajorVersion());
            id.setRevision(old.getId().getRevision() + 1);
        }
    }

    /*
    1. direct pass method argument
    2. setDAO :(
    3. implicitly pass context
     */
    public WfRunModel startNewRun(RunWfRequestModel evt, ProcessorExecutionContext processorContext) {
        CommandModel currentCommand = processorContext.currentCommand();
        GetableManager getableManager = processorContext.getableManager();
        WfRunModel out = new WfRunModel(processorContext);
        out.setId(new WfRunIdModel(evt.getId()));
        if (evt.getParentWfRunId() != null) out.getId().setParentWfRunId(evt.getParentWfRunId());

        out.setWfSpec(this);
        out.setWfSpecId(getObjectId());
        out.startTime = currentCommand.getTime();
        out.transitionTo(LHStatus.RUNNING);

        out.startThread(
                entrypointThreadName, currentCommand.getTime(), null, evt.getVariables(), ThreadType.ENTRYPOINT);
        getableManager.put(out);
        return out;
    }

    /*
     * Validates that the parent reference is a valid WfSpec. It doesn't do any
     * checking of variables, though. That is a future feature we will add in 1.0
     * or 1.1
     */
    private WfSpecModel getParentWfSpec(MetadataCommandExecution ctx) {
        WfSpecModel parent =
                ctx.service().getWfSpec(parentWfSpec.getWfSpecName(), parentWfSpec.getWfSpecMajorVersion(), 0);
        if (parent == null) {
            throw new LHApiException(
                    Status.INVALID_ARGUMENT,
                    "Provided spec refers to nonexistent parent wfSpec %s".formatted(parentWfSpec.getWfSpecName()));
        }
        return parent;
    }

    public static WfSpecId parseId(String fullId) {
        return ((WfSpecIdModel) ObjectIdModel.fromString(fullId, WfSpecIdModel.class))
                .toProto()
                .build();
    }
}

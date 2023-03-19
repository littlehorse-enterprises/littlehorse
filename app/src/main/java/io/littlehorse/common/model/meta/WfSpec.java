package io.littlehorse.common.model.meta;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHDAO;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.GETable;
import io.littlehorse.common.model.command.subcommand.RunWf;
import io.littlehorse.common.model.wfrun.WfRun;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.jlib.common.proto.LHStatusPb;
import io.littlehorse.jlib.common.proto.ThreadSpecPb;
import io.littlehorse.jlib.common.proto.ThreadTypePb;
import io.littlehorse.jlib.common.proto.WfSpecIdPb;
import io.littlehorse.jlib.common.proto.WfSpecPb;
import io.littlehorse.jlib.common.proto.WfSpecPbOrBuilder;
import io.littlehorse.server.streamsimpl.storeinternals.utils.StoreUtils;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;

public class WfSpec extends GETable<WfSpecPb> {

    public String name;
    public int version;
    public Date createdAt;
    public long lastOffset;

    public Map<String, ThreadSpec> threadSpecs;

    public String entrypointThreadName;
    public LHStatusPb status;

    @JsonIgnore
    private Map<String, String> varToThreadSpec;

    @JsonIgnore
    private boolean initializedVarToThreadSpec;

    public String getName() {
        return name;
    }

    /*
     * This determines ordering. In order to ensure quick lookups for the WfSpec with
     * a given name and the newest version, we need the versions to be ordered
     * **lexicographically**, not just numerically.
     */
    public String getObjectId() {
        return getObjectId(name, version);
    }

    public static String getObjectId(String name, int version) {
        return LHUtil.getCompositeId(name, LHUtil.toLHDbVersionFormat(version));
    }

    public static String getFullPrefixByName(String name) {
        return StoreUtils.getFullStoreKey(name + "/", WfSpec.class);
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getPartitionKey() {
        return LHConstants.META_PARTITION_KEY;
    }

    public WfSpec() {
        threadSpecs = new HashMap<>();
        varToThreadSpec = new HashMap<>();
        initializedVarToThreadSpec = false;
    }

    public void setLastUpdatedOffset(long newOffset) {
        lastOffset = newOffset;
    }

    public WfSpecPb.Builder toProto() {
        WfSpecPb.Builder out = WfSpecPb
            .newBuilder()
            .setVersion(version)
            .setCreatedAt(LHUtil.fromDate(createdAt))
            .setEntrypointThreadName(entrypointThreadName)
            .setStatus(status)
            .setName(name);

        if (threadSpecs != null) {
            for (Map.Entry<String, ThreadSpec> p : threadSpecs.entrySet()) {
                out.putThreadSpecs(p.getKey(), p.getValue().toProto().build());
            }
        }

        return out;
    }

    public void initFrom(MessageOrBuilder pr) {
        WfSpecPbOrBuilder proto = (WfSpecPbOrBuilder) pr;
        createdAt = LHUtil.fromProtoTs(proto.getCreatedAt());
        version = proto.getVersion();
        entrypointThreadName = proto.getEntrypointThreadName();
        status = proto.getStatus();
        name = proto.getName();

        for (Map.Entry<String, ThreadSpecPb> e : proto
            .getThreadSpecsMap()
            .entrySet()) {
            ThreadSpec ts = new ThreadSpec();
            ts.wfSpec = this;
            ts.name = e.getKey();
            ts.initFrom(e.getValue());
            threadSpecs.put(e.getKey(), ts);
        }
    }

    @JsonIgnore
    public Class<WfSpecPb> getProtoBaseClass() {
        return WfSpecPb.class;
    }

    public static WfSpec fromProto(WfSpecPbOrBuilder proto) {
        WfSpec out = new WfSpec();
        out.initFrom(proto);
        return out;
    }

    private void initializeVarToThreadSpec() {
        initializedVarToThreadSpec = true;
        for (ThreadSpec tspec : threadSpecs.values()) {
            for (VariableDef vd : tspec.variableDefs) {
                varToThreadSpec.put(vd.name, tspec.name);
            }
        }
    }

    public Pair<String, VariableDef> lookupVarDef(String name) {
        if (!initializedVarToThreadSpec) {
            initializeVarToThreadSpec();
        }
        String tspecName = varToThreadSpec.get(name);
        if (tspecName == null) return null;
        VariableDef out = threadSpecs.get(tspecName).localGetVarDef(name);
        if (out == null) return null;
        return Pair.of(tspecName, out);
    }

    public void validate(LHGlobalMetaStores dbClient, LHConfig config)
        throws LHValidationError {
        if (threadSpecs.get(entrypointThreadName) == null) {
            throw new LHValidationError(null, "Unknown entrypoint thread");
        }

        // Validate the variable definitions.
        // This will get tricky with interrupts, but...
        validateVariablesHelper();

        for (Map.Entry<String, ThreadSpec> e : threadSpecs.entrySet()) {
            ThreadSpec ts = e.getValue();
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
        for (ThreadSpec tspec : threadSpecs.values()) {
            out.addAll(tspec.getNodeExternalEventDefs());
        }
        return out;
    }

    @JsonIgnore
    public Map<String, VariableDef> getRequiredVariables() {
        return threadSpecs.get(entrypointThreadName).getRequiredInputVariables();
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
        for (ThreadSpec tspec : threadSpecs.values()) {
            // for (Map.Entry<String, VariableDef> e : tspec.variableDefs.entrySet()) {
            for (VariableDef vd : tspec.variableDefs) {
                String varName = vd.name;

                if (varToThreadSpec.containsKey(varName)) {
                    if (!varName.equals(LHConstants.EXT_EVT_HANDLER_VAR)) {
                        throw new LHValidationError(
                            null,
                            "Var name " +
                            varName +
                            " defined in threads " +
                            tspec.name +
                            " and " +
                            varToThreadSpec.get(varName)
                        );
                    }
                }
                varToThreadSpec.put(varName, name);
            }
        }

        // Seen Vars is now loaded.
        initializeVarToThreadSpec();

        for (ThreadSpec tspec : threadSpecs.values()) {
            for (String varName : tspec.getRequiredVariableNames()) {
                if (!varToThreadSpec.containsKey(varName)) {
                    throw new LHValidationError(
                        null,
                        "Thread " + tspec.name + " refers to missing var " + varName
                    );
                }
            }
        }
    }

    public WfRun startNewRun(RunWf evt, LHDAO dao) {
        WfRun out = new WfRun();
        out.cmdDao = dao;
        out.id = evt.id;

        out.wfSpec = this;
        out.wfSpecVersion = version;
        out.wfSpecName = name;
        out.startTime = dao.getEventTime();
        out.status = LHStatusPb.RUNNING;

        out.startThread(
            entrypointThreadName,
            dao.getEventTime(),
            null,
            evt.variables,
            ThreadTypePb.ENTRYPOINT
        );

        dao.saveWfRun(out);

        return out;
    }

    public static WfSpecIdPb parseId(String fullId) {
        String[] split = fullId.split("/");
        return WfSpecIdPb
            .newBuilder()
            .setName(split[0])
            .setVersion(Integer.valueOf(split[1]))
            .build();
    }

    public static String getObjectId(WfSpecIdPb id) {
        return getObjectId(id.getName(), id.getVersion());
    }
}

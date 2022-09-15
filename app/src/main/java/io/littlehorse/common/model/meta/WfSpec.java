package io.littlehorse.common.model.meta;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.exceptions.LHSerdeError;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.GlobalPOSTable;
import io.littlehorse.common.model.POSTable;
import io.littlehorse.common.model.event.TaskScheduleRequest;
import io.littlehorse.common.model.event.WfRunEvent;
import io.littlehorse.common.model.observability.ObservabilityEvent;
import io.littlehorse.common.model.observability.RunStartOe;
import io.littlehorse.common.model.server.Tag;
import io.littlehorse.common.model.wfrun.LHTimer;
import io.littlehorse.common.model.wfrun.WfRun;
import io.littlehorse.common.proto.LHStatusPb;
import io.littlehorse.common.proto.ThreadSpecPb;
import io.littlehorse.common.proto.WfSpecPb;
import io.littlehorse.common.proto.WfSpecPbOrBuilder;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.server.processors.util.WfRunStoreAccess;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;

public class WfSpec extends GlobalPOSTable<WfSpecPbOrBuilder> {

    public String id;
    public String name;
    public Date createdAt;
    public Date updatedAt;
    public long lastOffset;

    public Map<String, ThreadSpec> threadSpecs;

    public String entrypointThreadName;
    public LHStatusPb status;

    public String getName() {
        return name;
    }

    public String getObjectId() {
        if (id.equals("")) {
            id = LHUtil.generateGuid();
        }
        return id;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getPartitionKey() {
        return getObjectId();
    }

    public WfSpec() {
        threadSpecs = new HashMap<>();
    }

    public long getLastUpdatedOffset() {
        return lastOffset;
    }

    public void setLastUpdatedOffset(long newOffset) {
        lastOffset = newOffset;
    }

    public WfSpecPb.Builder toProto() {
        WfSpecPb.Builder out = WfSpecPb
            .newBuilder()
            .setId(id)
            .setCreatedAt(LHUtil.fromDate(createdAt))
            .setUpdatedAt(LHUtil.fromDate(updatedAt))
            .setEntrypointThreadName(entrypointThreadName)
            .setStatus(status)
            .setName(name)
            .setLastUpdatedOffset(lastOffset);

        if (threadSpecs != null) {
            for (Map.Entry<String, ThreadSpec> p : threadSpecs.entrySet()) {
                out.putThreadSpecs(p.getKey(), p.getValue().toProto().build());
            }
        }

        return out;
    }

    public void initFrom(MessageOrBuilder pr) throws LHSerdeError {
        WfSpecPbOrBuilder proto = (WfSpecPbOrBuilder) pr;
        createdAt = LHUtil.fromProtoTs(proto.getCreatedAt());
        id = proto.getId();
        updatedAt = LHUtil.fromProtoTs(proto.getUpdatedAt());
        entrypointThreadName = proto.getEntrypointThreadName();
        status = proto.getStatus();
        name = proto.getName();
        lastOffset = proto.getLastUpdatedOffset();

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

    public static WfSpec fromProto(WfSpecPbOrBuilder proto)
        throws LHSerdeError {
        WfSpec out = new WfSpec();
        out.initFrom(proto);
        return out;
    }

    @JsonIgnore
    public boolean handleDelete() {
        return true;
    }

    @JsonIgnore
    public void handlePost(
        POSTable<WfSpecPbOrBuilder> old,
        LHGlobalMetaStores dbClient,
        LHConfig config
    ) throws LHValidationError {
        // This does a lot of validation; therefore, it's quite complicated.
        if (old != null) {
            throw new LHValidationError(
                null,
                "Mutating WfSpec not yet supported"
            );
        }

        validate(dbClient, config);
    }

    private void validate(LHGlobalMetaStores dbClient, LHConfig config)
        throws LHValidationError {
        if (threadSpecs.get(entrypointThreadName) == null) {
            throw new LHValidationError(null, "Unknown entrypoint thread");
        }

        Map<String, Map<String, VariableDef>> allVarDefs = new HashMap<>();

        for (Map.Entry<String, ThreadSpec> e : threadSpecs.entrySet()) {
            ThreadSpec ts = e.getValue();
            ts.validate(dbClient, config);

            allVarDefs.put(ts.name, ts.variableDefs);
        }

        // Validate the variable definitions.
        ensureNoDuplicateVarNames(allVarDefs);
        HashSet<String> seenThreads = new HashSet<String>();
        HashMap<String, String> visibleVariables = new HashMap<>();
        // This will get tricky with interrupts, but...
        validateVariablesHelper(
            seenThreads,
            visibleVariables,
            this.entrypointThreadName
        );
    }

    @JsonIgnore
    public Map<String, VariableDef> getRequiredVariables() {
        return threadSpecs.get(entrypointThreadName).getRequiredVariables();
    }

    private void validateVariablesHelper(
        Set<String> seenThreads,
        Map<String, String> seenVars,
        String threadName
    ) throws LHValidationError {
        if (seenThreads.contains(threadName)) {
            return;
        }

        ThreadSpec thread = this.threadSpecs.get(threadName);
        for (String varName : thread.variableDefs.keySet()) {
            if (seenVars.containsKey(varName)) {
                throw new LHValidationError(
                    null,
                    "Variable " +
                    varName +
                    " defined again in child thread " +
                    threadName +
                    " after being defined in thread " +
                    seenVars.get(varName)
                );
            }
            seenVars.put(varName, threadName);
        }

        // // Now iterate through all of the tasks in this thread and see if the
        // // variables are defined.
        // for (Node node: thread.nodes.values()) {
        //     for (String varName: node.variables.keySet()) {
        //         VariableAssignment assign = node.variables.get(varName);
        //         if (assign.wfRunVariableName == null) {
        //             continue;
        //         }
        //         if (!seenVars.containsKey(assign.wfRunVariableName)) {
        //             throw new LHValidationError(
        //                 "Variable " + varName + "refers to wfRunVariable named " +
        //                 assign.wfRunVariableName + ", which is either not defined"
        //                 + " or not in scope for thread " + thread.name + " on node "
        //                 + node.name
        //             );
        //         }
        //     }

        //     for (String varName: node.variableMutations.keySet()) {
        //         if (!seenVars.containsKey(varName)) {
        //             throw new LHValidationError(
        //                 "Variable " + varName + " either not defined or not in " +
        //                 "scope for thread " + thread.name + " on node " + node.name
        //             );
        //         }
        //     }

        //     if (node.timeoutSeconds != null) {
        //         VariableAssignment assn = node.timeoutSeconds;
        //         if (assn.wfRunVariableName != null) {
        //             if (!seenVars.containsKey(node.timeoutSeconds.wfRunVariableName)) {
        //                 throw new LHValidationError(
        //                     "refers to wfRunVariable named " +
        //                     assn.wfRunVariableName + ", which is either not defined"
        //                     + " or not in scope for thread " + thread.name +
        //                     " on node " + node.name
        //                 );
        //             }
        //         }
        //     }
        // }

        // // Now process every potential child thread.
        // for (Node node: thread.nodes.values()) {
        //     if (node.nodeType == NodeType.SPAWN_THREAD) {
        //         String nextThreadName = node.threadSpawnThreadSpecName;
        //         validateVariablesHelper(seenThreads, seenVars, nextThreadName);
        //     }
        //     if (node.baseExceptionhandler != null) {
        //         String threadSpec = node.baseExceptionhandler.handlerThreadSpecName;
        //         if (threadSpec != null) {
        //             validateVariablesHelper(seenThreads, seenVars, threadSpec);
        //         }
        //     }
        // }

        // // Due to recursive backtracking, we need to remove the elements we added.
        // for (String varName: thread.variableDefs.keySet()) {
        //     seenVars.remove(varName);
        // }
        seenThreads.remove(threadName);
    }

    private void ensureNoDuplicateVarNames(
        Map<String, Map<String, VariableDef>> allVarDefs
    ) throws LHValidationError {
        HashSet<String> seen = new HashSet<String>();
        for (Map.Entry<String, Map<String, VariableDef>> e : allVarDefs.entrySet()) {
            for (String varName : e.getValue().keySet()) {
                if (seen.contains(varName)) {
                    throw new LHValidationError(
                        null,
                        "Variable " + varName + " defined twice! No bueno."
                    );
                }
                seen.add(varName);
            }
        }
    }

    public List<Tag> getTags() {
        List<Tag> out = Arrays.asList(new Tag(this, Pair.of("name", name)));

        return out;
    }

    public WfRun startNewRun(
        WfRunEvent e,
        List<TaskScheduleRequest> tasksToSchedule,
        List<LHTimer> timersToSchedule,
        WfRunStoreAccess wsa
    ) {
        WfRun out = new WfRun();
        out.stores = wsa;
        out.id = e.runRequest.wfRunId;
        out.oEvents.wfRunId = out.id;

        out.wfSpec = this;
        out.tasksToSchedule = tasksToSchedule;
        out.timersToSchedule = timersToSchedule;
        out.wfSpecId = id;
        out.wfSpecName = name;
        out.startTime = e.time;
        out.status = LHStatusPb.RUNNING;
        out.oEvents.add(
            new ObservabilityEvent(new RunStartOe(id, name), e.time)
        );

        out.startThread(
            entrypointThreadName,
            e.time,
            null,
            e.runRequest.variables
        );

        return out;
    }
}

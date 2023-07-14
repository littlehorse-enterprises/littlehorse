package io.littlehorse.sdk.wfsdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.util.JsonFormat;
import io.littlehorse.sdk.client.LHClient;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.exception.LHApiError;
import io.littlehorse.sdk.common.proto.PutExternalEventDefPb;
import io.littlehorse.sdk.common.proto.PutTaskDefPb;
import io.littlehorse.sdk.common.proto.PutWfSpecPb;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

/**
 * The Workflow class represents a `WfSpec` object in the API.
 */
@Slf4j
public abstract class Workflow {

    protected ThreadFunc entrypointThread;
    protected String name;
    protected PutWfSpecPb.Builder spec;
    protected Map<String, ThreadFunc> threadFuncs;

    /**
     * Internal constructor used by WorkflowImpl.
     * @param name name of `WfSpec`.
     * @param entrypointThreadFunc is the entrypoint thread function.
     */
    protected Workflow(String name, ThreadFunc entrypointThreadFunc) {
        this.threadFuncs = new HashMap<>();
        this.entrypointThread = entrypointThreadFunc;
        this.name = name;
        this.spec = PutWfSpecPb.newBuilder().setName(name);
    }

    /**
     * Gets the workflow name passed at {@link #newWorkflow(String, ThreadFunc)}
     * @return the Workflow name
     */
    public String getName() {
        return name;
    }

    /**
     * Add the hours of life that the workflow will have in the system
     * @param retentionHours are the hours in which the workflow will live in the system
     */
    public void withRetentionHours(int retentionHours) {
        if (retentionHours < 1) {
            throw new IllegalArgumentException(
                "You must set a value at least 1 or greater"
            );
        }
        this.spec.setRetentionHours(retentionHours);
    }

    /**
     * Creates a new Workflow with the provided name and entrypoint thread function.
     * @param name is the name of the `WfSpec`.
     * @param entrypointThreadFunc is the ThreadFunc for the entrypoint ThreadSpec.
     * @return a Workflow.
     */
    public static Workflow newWorkflow(String name, ThreadFunc entrypointThreadFunc) {
        return new WorkflowImpl(name, entrypointThreadFunc);
    }

    /**
     * Compiles this Workflow into a `WfSpec`.
     * @return a `PutWfSpecPb` that can be used for the gRPC putWfSpec() call.
     */
    public abstract PutWfSpecPb compileWorkflow();

    /**
     * Creates a set of all TaskDef's that need to be created for this WfSpec,
     * determined by calls to ThreadBuilder::executeAndRegisterTaskDef().
     * @return a Set of PutTaskDefPbs containing a PutTaskDef for every auto-generated
     * TaskDef in this `WfSpec`.
     */
    public abstract Set<PutTaskDefPb> compileTaskDefs();

    /**
     * Returns the names of all `TaskDef`s used by this workflow.
     * @return a Set of Strings containing the names of all `TaskDef`s used by
     * this workflow.
     */
    public abstract Set<String> getRequiredTaskDefNames();

    /**
     * Returns a set of all objects that were passed to executeAndRegisterTaskDef().
     * @return a set of all Task Worker objects that were passed into
     * ThreadBuilder::executeAndRegisterTaskDef().
     */
    public abstract Set<Object> getTaskExecutables();

    /**
     * Returns the names of all `ExternalEventDef`s used by this workflow. Includes
     * ExternalEventDefs used for Interrupts or for EXTERNAL_EVENT nodes.
     * @return a Set of Strings containing the names of all `ExternalEventDef`s used by
     * this workflow.
     */
    public abstract Set<String> getRequiredExternalEventDefNames();

    /**
     * Returns the associated PutWfSpecPb in JSON form.
     * @return the associated PutWfSpecPb in JSON form.
     */
    public String compileWfToJson() {
        PutWfSpecPb wfSpec = compileWorkflow();
        try {
            return JsonFormat.printer().includingDefaultValueFields().print(wfSpec);
        } catch (InvalidProtocolBufferException exn) {
            throw new RuntimeException(exn);
        }
    }

    /**
     * Creates a list of all TaskDef's that need to be created for this WfSpec,
     * determined by calls to ThreadBuilder::executeAndRegisterTaskDef().
     * @return a List containing the containing a PutTaskDef in Json form (String)
     * for every auto-generated TaskDef in this `WfSpec`.
     */
    public List<String> compileTaskDefsToJson() {
        Set<PutTaskDefPb> taskDefs = compileTaskDefs();
        List<String> out = new ArrayList<>();
        try {
            for (PutTaskDefPb ptd : taskDefs) {
                out.add(
                    JsonFormat.printer().includingDefaultValueFields().print(ptd)
                );
            }
            return out;
        } catch (InvalidProtocolBufferException exn) {
            throw new RuntimeException(exn);
        }
    }

    /**
     * Checks if the WfSpec exists
     * @param client is an LHClient.
     * @return true if the workflow spec is registered or false otherwise
     * @throws LHApiError if the call fails.
     */
    public boolean doesWfSpecExist(LHClient client) throws LHApiError {
        return client.getWfSpec(name) != null;
    }

    /**
     * Checks if the WfSpec exists for a given version
     * @param client is an LHClient.
     * @param version workflow version
     * @return true if the workflow spec is registered for this version or false otherwise
     * @throws LHApiError
     */
    public boolean doesWfSpecExist(LHClient client, Integer version)
        throws LHApiError {
        return client.getWfSpec(name, version) != null;
    }

    /**
     * Deploys the WfSpec object to the LH Server.
     * Registering the WfSpec via Workflow::registerWfSpec()
     * is the same as client.putWfSpec(workflow.compileWorkflow()).
     * @param client is an LHClient.
     * @throws LHApiError if the call fails.
     */
    public void registerWfSpec(LHClient client) throws LHApiError {
        log.info(
            "Creating wfSpec:\n {}",
            LHLibUtil.protoToJson(client.putWfSpec(compileWorkflow()))
        );
    }

    /**
     * Writes out all PutTaskDefPb, PutExternalEventDefPb, and PutWfSpecPb
     * in JSON form in a directory.
     * @param directory is the location to save the resources.
     */
    public void compileAndSaveToDisk(String directory) {
        for (PutTaskDefPb putTaskDef : compileTaskDefs()) {
            String fileName = putTaskDef.getName() + "-taskdef.json";
            log.info("Saving TaskDef to {}", fileName);
            saveProtoToFile(directory, fileName, putTaskDef);
        }

        for (String eedName : getRequiredExternalEventDefNames()) {
            String fileName = eedName + "-extevtdef.json";
            log.info("Saving ExternalEventDef to {}", fileName);
            saveProtoToFile(
                directory,
                fileName,
                PutExternalEventDefPb.newBuilder().setName(eedName)
            );
        }

        PutWfSpecPb wf = compileWorkflow();
        String wfFileName = wf.getName() + "-wfspec.json";
        log.info("Saving WfSpec to {}", wfFileName);
        saveProtoToFile(directory, wfFileName, wf);
    }

    private static void saveProtoToFile(
        String directory,
        String fileName,
        MessageOrBuilder content
    ) {
        Path path = Paths.get(directory, fileName);
        try {
            File file = new File(path.toString());
            file.getParentFile().mkdirs();
            Files.write(path, LHLibUtil.protoToJson(content).getBytes());
        } catch (IOException exn) {
            throw new RuntimeException(exn);
        }
    }
}

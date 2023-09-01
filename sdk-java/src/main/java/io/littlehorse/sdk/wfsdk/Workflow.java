package io.littlehorse.sdk.wfsdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.util.JsonFormat;
import io.grpc.Status.Code;
import io.grpc.StatusRuntimeException;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.GetLatestWfSpecRequest;
import io.littlehorse.sdk.common.proto.LHPublicApiGrpc.LHPublicApiBlockingStub;
import io.littlehorse.sdk.common.proto.PutWfSpecRequest;
import io.littlehorse.sdk.common.proto.WfSpecId;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

/** The Workflow class represents a `WfSpec` object in the API. */
@Slf4j
public abstract class Workflow {

    protected ThreadFunc entrypointThread;
    protected String name;
    protected PutWfSpecRequest.Builder spec;
    protected Queue<Pair<String, ThreadFunc>> threadFuncs;

    /**
     * Internal constructor used by WorkflowImpl.
     *
     * @param name name of `WfSpec`.
     * @param entrypointThreadFunc is the entrypoint thread function.
     */
    protected Workflow(String name, ThreadFunc entrypointThreadFunc) {
        this.threadFuncs = new LinkedList<>();
        this.entrypointThread = entrypointThreadFunc;
        this.name = name;
        this.spec = PutWfSpecRequest.newBuilder().setName(name);
    }

    /**
     * Gets the workflow name passed at {@link #newWorkflow(String, ThreadFunc)}
     *
     * @return the Workflow name
     */
    public String getName() {
        return name;
    }

    /**
     * Add the hours of life that the workflow will have in the system
     *
     * @param retentionHours are the hours in which the workflow will live in the system
     */
    public void withRetentionHours(int retentionHours) {
        if (retentionHours < 1) {
            throw new IllegalArgumentException("You must set a value at least 1 or greater");
        }
        this.spec.setRetentionHours(retentionHours);
    }

    /**
     * Creates a new Workflow with the provided name and entrypoint thread function.
     *
     * @param name is the name of the `WfSpec`.
     * @param entrypointThreadFunc is the ThreadFunc for the entrypoint ThreadSpec.
     * @return a Workflow.
     */
    public static Workflow newWorkflow(String name, ThreadFunc entrypointThreadFunc) {
        return new WorkflowImpl(name, entrypointThreadFunc);
    }

    /**
     * Compiles this Workflow into a `WfSpec`.
     *
     * @return a `PutWfSpecRequest` that can be used for the gRPC putWfSpec() call.
     */
    public abstract PutWfSpecRequest compileWorkflow();

    /**
     * Returns the names of all `TaskDef`s used by this workflow.
     *
     * @return a Set of Strings containing the names of all `TaskDef`s used by this workflow.
     */
    public abstract Set<String> getRequiredTaskDefNames();

    /**
     * Returns the names of all `ExternalEventDef`s used by this workflow. Includes
     * ExternalEventDefs used for Interrupts or for EXTERNAL_EVENT nodes.
     *
     * @return a Set of Strings containing the names of all `ExternalEventDef`s used by this
     *     workflow.
     */
    public abstract Set<String> getRequiredExternalEventDefNames();

    /**
     * Returns the associated PutWfSpecRequest in JSON form.
     *
     * @return the associated PutWfSpecRequest in JSON form.
     */
    public String compileWfToJson() {
        PutWfSpecRequest wfSpec = compileWorkflow();
        try {
            return JsonFormat.printer().includingDefaultValueFields().print(wfSpec);
        } catch (InvalidProtocolBufferException exn) {
            throw new RuntimeException(exn);
        }
    }

    /**
     * Checks if the WfSpec exists
     *
     * @param client is an LHClient.
     * @return true if the workflow spec is registered or false otherwise
     */
    public boolean doesWfSpecExist(LHPublicApiBlockingStub client) {
        try {
            client.getLatestWfSpec(
                    GetLatestWfSpecRequest.newBuilder().setName(name).build());
            return true;
        } catch (StatusRuntimeException exn) {
            if (exn.getStatus().getCode() == Code.NOT_FOUND) {
                return false;
            }

            throw exn;
        }
    }

    /**
     * Checks if the WfSpec exists for a given version
     *
     * @param client is an LHClient.
     * @param version workflow version
     * @return true if the workflow spec is registered for this version or false otherwise
     */
    public boolean doesWfSpecExist(LHPublicApiBlockingStub client, Integer version) {
        if (version == null) return doesWfSpecExist(client);

        try {
            client.getWfSpec(
                    WfSpecId.newBuilder().setName(name).setVersion(version).build());
            return true;
        } catch (StatusRuntimeException exn) {
            if (exn.getStatus().getCode() == Code.NOT_FOUND) {
                return false;
            }

            throw exn;
        }
    }

    /**
     * Deploys the WfSpec object to the LH Server. Registering the WfSpec via
     * Workflow::registerWfSpec() is the same as client.putWfSpec(workflow.compileWorkflow()).
     *
     * @param client is an LHClient.
     */
    public void registerWfSpec(LHPublicApiBlockingStub client) {
        log.info("Creating wfSpec:\n {}", LHLibUtil.protoToJson(client.putWfSpec(compileWorkflow())));
    }

    /**
     * Writes out the PutWfSpecRequest in JSON form in a directory.
     *
     * @param directory is the location to save the resources.
     */
    public void compileAndSaveToDisk(String directory) {
        PutWfSpecRequest wf = compileWorkflow();
        String wfFileName = wf.getName() + "-wfspec.json";
        log.info("Saving WfSpec to {}", wfFileName);
        saveProtoToFile(directory, wfFileName, wf);
    }

    private static void saveProtoToFile(String directory, String fileName, MessageOrBuilder content) {
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

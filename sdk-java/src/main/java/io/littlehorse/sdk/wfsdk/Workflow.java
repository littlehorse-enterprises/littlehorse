package io.littlehorse.sdk.wfsdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import io.grpc.Status.Code;
import io.grpc.StatusRuntimeException;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.AllowedUpdateType;
import io.littlehorse.sdk.common.proto.ExponentialBackoffRetryPolicy;
import io.littlehorse.sdk.common.proto.GetLatestWfSpecRequest;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.PutExternalEventDefRequest;
import io.littlehorse.sdk.common.proto.PutWorkflowEventDefRequest;
import io.littlehorse.sdk.common.proto.PutWfSpecRequest;
import io.littlehorse.sdk.common.proto.ThreadRetentionPolicy;
import io.littlehorse.sdk.common.proto.WfSpecId;
import io.littlehorse.sdk.common.proto.WorkflowRetentionPolicy;
import io.littlehorse.sdk.wfsdk.internal.ExternalEventNodeOutputImpl;
import io.littlehorse.sdk.wfsdk.internal.ThrowEventNodeOutputImpl;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
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
    protected Integer defaultTaskTimeout;
    protected WorkflowRetentionPolicy wfRetentionPolicy;
    protected ThreadRetentionPolicy defaultThreadRetentionPolicy;
    protected String parentWfSpecName;

    protected ExponentialBackoffRetryPolicy defaultExponentialBackoff;
    protected int defaultSimpleRetries;
    protected Set<ExternalEventNodeOutputImpl> externalEventsToRegister;
    protected Set<ThrowEventNodeOutputImpl> workflowEventsToRegister;

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
        this.externalEventsToRegister = new HashSet<>();
        this.workflowEventsToRegister = new HashSet<>();
    }

    /**
     * Makes this a child workflow.
     * @param parentWfSpecName is the name of the parent wfSpec.
     */
    public void setParent(String parentWfSpecName) {
        this.parentWfSpecName = parentWfSpecName;
    }

    /**
     * Sets the default timeout for all TaskRun's in this workflow.
     * @param timeoutSeconds is the value for the timeout to set.
     */
    public void setDefaultTaskTimeout(int timeoutSeconds) {
        this.defaultTaskTimeout = timeoutSeconds;
    }

    /**
     * Returns the default task timeout, or null if it is not set.
     * @return the default task timeout for this Workflow.
     */
    public Integer getDefaultTaskTimeout() {
        return defaultTaskTimeout;
    }

    /**
     * Tells the Workflow to configure (by default) a Simple Retry Policy for every Task Node. Passing
     * a value of '1' means that there will be one retry upon failure. Retries are scheduled immediately
     * without delay.
     *
     * Can be overriden by setting the retry policy on the WorkflowThread or TaskNodeOutput level.
     * @param defaultSimpleRetries is the number ofretries to attempt.
     */
    public void setDefaultTaskRetries(int defaultSimpleRetries) {
        if (defaultSimpleRetries < 0) {
            throw new IllegalArgumentException("Cannot have negative retries!");
        }
        this.defaultSimpleRetries = defaultSimpleRetries;
    }

    /**
     * Tells the Workflow to configure (by default) the specified ExponentialBackoffRetryPolicy as
     * the retry policy.
     *
     * Can be overriden by setting the retry policy on the WorkflowThread or TaskNodeOutput level.
     * @param defaultPolicy is the Exponential Backoff Retry Policy to configure by default for all
     * Task Nodes.
     */
    public void setDefaultTaskExponentialBackoffPolicy(ExponentialBackoffRetryPolicy defaultPolicy) {
        this.defaultExponentialBackoff = defaultPolicy;
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
     * Sets the retention policy for all WfRun's created by this WfSpec.
     * @param policy is the Workflow Retention Policy.
     * @return this Workflow.
     */
    public Workflow withRetentionPolicy(WorkflowRetentionPolicy policy) {
        this.wfRetentionPolicy = policy;
        return this;
    }

    /**
     * Sets the retention policy for all ThreadRun's belong to this WfSpec.
     *
     * Note that each Thread can override the configured Retention Policy by
     * using WorkflowThread#withRetentionPolicy.
     * @param policy is the Workflow Retention Policy.
     * @return this Workflow.
     */
    public Workflow withDefaultThreadRetentionPolicy(ThreadRetentionPolicy policy) {
        this.defaultThreadRetentionPolicy = policy;
        return this;
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
     * Defines the type of update to perform when saving the WfSpec:
     * AllowedUpdateType.ALL (Default): Creates a new WfSpec with a different version (either major or revision).
     * AllowedUpdateType.MINOR_REVISION_ONLY: Creates a new WfSpec with a different revision if the change is a major version it fails.
     * AllowedUpdateType.NONE: Fail with the ALREADY_EXISTS response code.
     * @param allowedUpdateType
     * @return this Worflow
     */
    public Workflow withUpdateType(AllowedUpdateType allowedUpdateType) {
        this.spec.setAllowedUpdates(allowedUpdateType);
        return this;
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
     * Returns ExternalEventDef registrations declared via ExternalEventNodeOutput#registeredAs.
     *
     * @return a Set of PutExternalEventDefRequest for ExternalEventDefs that will be registered
     *     with this workflow.
     */
    public abstract Set<PutExternalEventDefRequest> getExternalEventDefsToRegister();

    /**
     * Returns the names of all `WorkflowEventDef`s used by this workflow.
     *
     * @return a Set of Strings containing the names of all `WorkflowEventDef`s thrown by this
     *      workflow.
     */
    public abstract Set<String> getRequiredWorkflowEventDefNames();

    /**
     * Returns WorkflowEventDef registrations declared via ThrowEventNodeOutput#registeredAs.
     *
     * @return a Set of PutWorkflowEventDefRequest for WorkflowEventDefs that will be registered
     *     with this workflow.
     */
    public abstract Set<PutWorkflowEventDefRequest> getWorkflowEventDefsToRegister();

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
    public boolean doesWfSpecExist(LittleHorseBlockingStub client) {
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
     * @param majorVersion the workflow Major Version
     * @return true if the workflow spec is registered for this Major Version or false otherwise
     */
    public boolean doesWfSpecExist(LittleHorseBlockingStub client, Integer majorVersion) {
        // TODO: LH-282, support revision versioning here.
        if (majorVersion == null) return doesWfSpecExist(client);

        try {
            client.getWfSpec(WfSpecId.newBuilder()
                    .setName(name)
                    .setMajorVersion(majorVersion)
                    .build());
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
    public abstract void registerWfSpec(LittleHorseBlockingStub client);

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

    private static void saveProtoToFile(String directory, String fileName, Message content) {
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

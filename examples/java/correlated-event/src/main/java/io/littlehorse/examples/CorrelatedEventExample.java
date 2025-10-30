package io.littlehorse.examples;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.CorrelatedEventConfig;
import io.littlehorse.sdk.common.proto.ExternalEventDefId;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.PutCorrelatedEventRequest;
import io.littlehorse.sdk.common.proto.PutExternalEventDefRequest;
import io.littlehorse.sdk.common.proto.ReturnType;
import io.littlehorse.sdk.common.proto.RunWfRequest;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.WfRun;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * This example demonstrates the asynchronous ExternalEvent functionality.
 * We will use "thread.waitForEvent" to wait for an external event, then when it arrives
 * it executes the task "greet".
 */
public class CorrelatedEventExample {

    public static final String WF_NAME = "correlated-events";
    public static final String EVENT_NAME = "document-signed";

    private static final Logger log = LoggerFactory.getLogger(CorrelatedEventExample.class);

    private static Workflow getWorkflow() {
        return Workflow.newWorkflow(WF_NAME, wf -> {
            WfRunVariable documentId = wf.declareStr("document-id");
            wf.waitForEvent(EVENT_NAME).withCorrelationId(documentId);
        });
    }

    public static Properties getConfigProps() throws IOException {
        Properties props = new Properties();
        File configPath = Path.of(System.getProperty("user.home"), ".config/littlehorse.config")
                .toFile();
        if (configPath.exists()) {
            props.load(new FileInputStream(configPath));
        }
        return props;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        // Let's prepare the configurations
        Properties props = getConfigProps();
        LHConfig config = new LHConfig(props);
        LittleHorseBlockingStub client = config.getBlockingStub();

        String correlationKey = args.length == 0 ? "some-document" : args[0];

        client.putExternalEventDef(PutExternalEventDefRequest.newBuilder()
                .setName(EVENT_NAME)
                .setContentType(ReturnType.newBuilder()
                        .setReturnType(TypeDefinition.newBuilder().setPrimitiveType(VariableType.BOOL)))
                .setCorrelatedEventConfig(CorrelatedEventConfig.newBuilder().setDeleteAfterFirstCorrelation(false))
                .build());

        Workflow workflowGenerator = getWorkflow();
        workflowGenerator.registerWfSpec(client);

        // For fun, let's run the workflow
        Thread.sleep(1000);

        WfRun result = client.runWf(RunWfRequest.newBuilder()
                .setWfSpecName(WF_NAME)
                .putVariables("document-id", LHLibUtil.objToVarVal(correlationKey))
                .build());

        System.out.println(LHLibUtil.protoToJson(result));

        client.putCorrelatedEvent(PutCorrelatedEventRequest.newBuilder()
                .setExternalEventDefId(ExternalEventDefId.newBuilder().setName(EVENT_NAME))
                .setKey(correlationKey)
                .setContent(LHLibUtil.objToVarVal(true))
                .build());

        System.out.println(LHLibUtil.protoToJson(client.getWfRun(result.getId())));
    }
}

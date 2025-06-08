package io.littlehorse.examples;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.CorrelatedEventConfig;
import io.littlehorse.sdk.common.proto.PutExternalEventDefRequest;
import io.littlehorse.sdk.common.proto.ReturnType;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
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

    private static final Logger log = LoggerFactory.getLogger(
        CorrelatedEventExample.class
    );

    public static Properties getConfigProps() throws IOException {
        Properties props = new Properties();
        File configPath = Path.of(
            System.getProperty("user.home"),
            ".config/littlehorse.config"
        ).toFile();
        if(configPath.exists()){
            props.load(new FileInputStream(configPath));
        }
        return props;
    }

    public static void main(String[] args) throws IOException {
        // Let's prepare the configurations
        Properties props = getConfigProps();
        LHConfig config = new LHConfig(props);
        LittleHorseBlockingStub client = config.getBlockingStub();

        client.putExternalEventDef(PutExternalEventDefRequest.newBuilder()
                .setName("document-signed")
                .setContentType(ReturnType.newBuilder().setReturnType(TypeDefinition.newBuilder().setType(VariableType.BOOL)))
                .setCorrelatedEventConfig(CorrelatedEventConfig.newBuilder().setDeleteAfterFirstCorrelation(false))
                .build());
    }
}

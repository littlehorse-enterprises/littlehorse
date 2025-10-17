package io.littlehorse.examples;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.InlineStructDef;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.PutStructDefRequest;
import io.littlehorse.sdk.common.proto.StructDefCompatibilityType;
import io.littlehorse.sdk.common.proto.StructFieldDef;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.VariableValue;

/*
 * This is a simple example, which does two things:
 * 1. Declare an "input-name" variable of type String
 * 2. Pass that variable into the execution of the "greet" task.
 */
public class BasicExample {

    public static Properties getConfigProps() throws IOException {
        Properties props = new Properties();
        File configPath = Path.of(
                System.getProperty("user.home"),
                ".config/littlehorse.config").toFile();
        if (configPath.exists()) {
            props.load(new FileInputStream(configPath));
        }
        return props;
    }

    public static void main(String[] args) throws IOException {
        // Let's prepare the configurations
        Properties props = getConfigProps();
        LHConfig config = new LHConfig(props);
        LittleHorseBlockingStub client = config.getBlockingStub();

        client.putStructDef(PutStructDefRequest.newBuilder()
                .setName("car")
                .setDescription("A car is a StructDef that blah blah")
                .setAllowedUpdates(StructDefCompatibilityType.FULLY_COMPATIBLE_SCHEMA_UPDATES)
                .setStructDef(InlineStructDef.newBuilder()
                        .putFields("model", StructFieldDef.newBuilder()
                                .setFieldType(TypeDefinition.newBuilder()
                                        .setType(VariableType.STR))
                                .build())
                        .putFields("year", StructFieldDef.newBuilder()
                                .setFieldType(TypeDefinition.newBuilder()
                                        .setType(VariableType.INT))
                                .build())
                        .putFields("available", StructFieldDef.newBuilder()
                                .setFieldType(TypeDefinition.newBuilder()
                                        .setType(VariableType.BOOL))
                                .build())
                        .putFields("createdAt", StructFieldDef.newBuilder()
                                .setFieldType(TypeDefinition.newBuilder()
                                        .setType(VariableType.TIMESTAMP))
                                .build())
                        .putFields("ownerId", StructFieldDef.newBuilder()
                                .setFieldType(TypeDefinition.newBuilder()
                                        .setType(VariableType.INT))
                                .setDefaultValue(VariableValue.newBuilder()
                                        .setInt(0))
                                .build()))
                .build());
    }
}

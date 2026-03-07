package io.littlehorse.examples;

import io.littlehorse.sdk.common.proto.InlineStruct;
import io.littlehorse.sdk.common.proto.StructField;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.LHType;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyWorker {

    private static final Logger log = LoggerFactory.getLogger(MyWorker.class);

    @LHTaskMethod("inline-${model}-create-customer")
    @LHType(structDefName = "${customerStructName}")
    public InlineStruct createCustomer(String name, String email) {
        return InlineStruct.newBuilder()
                .putFields(
                        "id",
                        StructField.newBuilder()
                                .setValue(VariableValue.newBuilder()
                                        .setStr(UUID.randomUUID().toString()))
                                .build())
                .putFields(
                        "name",
                        StructField.newBuilder()
                                .setValue(VariableValue.newBuilder().setStr(name))
                                .build())
                .putFields(
                        "email",
                        StructField.newBuilder()
                                .setValue(VariableValue.newBuilder().setStr(email))
                                .build())
                .build();
    }

    @LHTaskMethod("inline-${model}-email-customer")
    public String emailCustomer(
            @LHType(structDefName = "${customerStructName}") InlineStruct customer, String content) {
        String email = customer.getFieldsOrThrow("email").getValue().getStr();
        String name = customer.getFieldsOrThrow("name").getValue().getStr();
        log.info("Sending '{}' to {} <{}>", content, name, email);
        return "sent";
    }
}

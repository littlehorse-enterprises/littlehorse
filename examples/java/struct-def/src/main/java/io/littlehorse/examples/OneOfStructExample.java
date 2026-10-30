package io.littlehorse.examples;

import com.google.protobuf.util.JsonFormat;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.wfsdk.internal.structdefutil.LHStructDefType;
import io.littlehorse.sdk.worker.LHStructDef;
import io.littlehorse.sdk.worker.LHStructOneOf;

/** Compare two Java APIs for the same schema and value, without connecting to a server. */
public class OneOfStructExample {
    @LHStructDef("payment")
    public record FlatPayment(
            String orderId,
            @LHStructOneOf(group = "method", required = true) String cardToken,
            @LHStructOneOf(group = "method", required = true) String bankAccountId,
            @LHStructOneOf(group = "method", required = true) String walletId) {}

    @LHStructOneOf(required = true)
    public record PaymentMethod(String cardToken, String bankAccountId, String walletId) {}

    @LHStructDef("payment")
    public record NestedPayment(String orderId, PaymentMethod method) {}

    public static void main(String[] args) throws Exception {
        var flatDefinition = new LHStructDefType(FlatPayment.class).toPutStructDefRequest();
        var nestedDefinition = new LHStructDefType(NestedPayment.class).toPutStructDefRequest();
        System.out.println("Identical schemas: " + flatDefinition.equals(nestedDefinition));
        System.out.println(JsonFormat.printer().print(flatDefinition));

        var flat = new FlatPayment("order-123", "tok_abc", null, null);
        var nested = new NestedPayment("order-123", new PaymentMethod("tok_abc", null, null));
        var flatValue = LHLibUtil.objToVarVal(flat);
        var nestedValue = LHLibUtil.objToVarVal(nested);
        System.out.println("Identical values: " + flatValue.equals(nestedValue));
        System.out.println(JsonFormat.printer().print(flatValue));

        // Either Java model can read the same protobuf value, including after a binary round trip.
        var received = VariableValue.parseFrom(flatValue.toByteArray());
        System.out.println(LHLibUtil.varValToObj(received, FlatPayment.class));
        System.out.println(LHLibUtil.varValToObj(received, NestedPayment.class));

        // Access stays natural for the model you chose.
        System.out.println(flat.cardToken());
        System.out.println(nested.method().cardToken());
    }
}

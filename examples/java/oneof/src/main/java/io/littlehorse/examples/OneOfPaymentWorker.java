package io.littlehorse.examples;

import io.littlehorse.sdk.worker.LHTaskMethod;

public class OneOfPaymentWorker {
    @LHTaskMethod("describe-payment-fields")
    public String describeFields(FlatPayment payment) {
        String method;
        if (payment.cardToken() != null) {
            method = "card";
        } else if (payment.bankAccountId() != null) {
            method = "bank account";
        } else {
            method = "wallet";
        }
        return "Order %s uses a %s".formatted(payment.orderId(), method);
    }

    @LHTaskMethod("describe-payment-helper")
    public String describeHelper(NestedPayment payment) {
        String method;
        if (payment.method().cardToken() != null) {
            method = "card";
        } else if (payment.method().bankAccountId() != null) {
            method = "bank account";
        } else {
            method = "wallet";
        }
        return "Order %s uses a %s".formatted(payment.orderId(), method);
    }
}

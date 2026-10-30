package io.littlehorse.examples;

import io.littlehorse.sdk.worker.LHStructDef;

@LHStructDef("payment")
public record NestedPayment(String orderId, PaymentMethod method) {}

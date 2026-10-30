package io.littlehorse.examples;

import io.littlehorse.sdk.worker.LHStructDef;
import io.littlehorse.sdk.worker.LHStructOneOf;

@LHStructDef("payment")
public record FlatPayment(
        String orderId,
        @LHStructOneOf(group = "method", required = true) String cardToken,
        @LHStructOneOf(group = "method", required = true) String bankAccountId,
        @LHStructOneOf(group = "method", required = true) String walletId) {}

package io.littlehorse.examples;

import io.littlehorse.sdk.worker.LHStructOneOf;

@LHStructOneOf(required = true)
public record PaymentMethod(String cardToken, String bankAccountId, String walletId) {}

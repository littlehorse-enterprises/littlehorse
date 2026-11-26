package io.littlehorse.examples;

import io.littlehorse.sdk.worker.LHStructDef;
import io.littlehorse.sdk.worker.LHStructOneOf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@LHStructDef("customer")
public class Customer {
    private String name;

    @LHStructOneOf(group = "contact")
    private String email;

    @LHStructOneOf(group = "contact")
    private String phoneNumber;
}

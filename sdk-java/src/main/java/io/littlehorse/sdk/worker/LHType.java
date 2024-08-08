package io.littlehorse.sdk.worker;

import io.littlehorse.sdk.common.proto.VariableType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface LHType {
    VariableType value();
}

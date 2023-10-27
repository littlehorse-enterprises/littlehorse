package io.littlehorse.server;

import io.littlehorse.common.proto.ACLAction;
import io.littlehorse.common.proto.ACLResource;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Authorize {

    ACLAction[] actions();

    ACLResource[] resources();
}

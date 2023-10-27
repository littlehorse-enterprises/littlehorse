package io.littlehorse.common;

import io.littlehorse.common.model.getable.global.acl.PrincipalModel;

public interface ServerContext {

    enum Scope {
        READ,
        PROCESSOR
    }

    Scope scope();

    PrincipalModel principal();

    String tenantId();
}

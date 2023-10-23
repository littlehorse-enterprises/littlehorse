package io.littlehorse.common;

import io.littlehorse.common.model.getable.global.acl.PrincipalModel;
import java.util.Optional;

public interface ServerContext {

    enum Scope {
        READ,
        PROCESSOR
    }

    Scope scope();

    Optional<PrincipalModel> principal();

    String tenantId();
}

package io.littlehorse.common;

import io.littlehorse.common.model.getable.global.acl.ServerACLModel;
import java.util.Collection;

public interface AuthorizationContext {

    enum Scope {
        READ,
        PROCESSOR
    }

    Scope scope();

    String principalId();

    String tenantId();

    Collection<ServerACLModel> acls();
}

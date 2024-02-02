package io.littlehorse.common;

import io.littlehorse.common.model.getable.global.acl.ServerACLModel;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import java.util.Collection;

/**
 * An instance of {@code AuthorizationContext} provides information about the current Principal within a specific context.
 * An {@code AuthorizationContext} is created in the following scenarios:
 * - When processing a Grpc request
 * - When processing an {@code io.littlehorse.common.model.SubCommand}
 * - When processing an {@code io.littlehorse.common.model.metadatacommand.MetadataSubCommand}
 * - When processing a scheduled task
 * You can obtain an instance of this interface through a DAO instance.
 */
public interface AuthorizationContext {

    /**
     * Current principal id in the context
     * @return not null
     */
    PrincipalIdModel principalId();

    /**
     * Current tenant id in the context
     * @return not null
     */
    TenantIdModel tenantId();

    /**
     * ACLs for the current principal in the context
     * @return might be empty
     */
    Collection<ServerACLModel> acls();

    /**
     * @return True if the principal is admin
     */
    boolean isAdmin();
}

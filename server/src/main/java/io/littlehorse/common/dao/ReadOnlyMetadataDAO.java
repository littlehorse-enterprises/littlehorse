package io.littlehorse.common.dao;

import com.google.protobuf.Message;
import io.littlehorse.common.ServerContext;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.model.getable.ObjectIdModel;
import io.littlehorse.common.model.getable.global.acl.PrincipalModel;
import io.littlehorse.common.model.getable.global.acl.TenantModel;
import io.littlehorse.common.model.getable.global.externaleventdef.ExternalEventDefModel;
import io.littlehorse.common.model.getable.global.taskdef.TaskDefModel;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.usertasks.UserTaskDefModel;
import java.util.List;

public interface ReadOnlyMetadataDAO {

    UserTaskDefModel getUserTaskDef(String name, Integer version);

    ExternalEventDefModel getExternalEventDef(String name);

    TaskDefModel getTaskDef(String name);

    WfSpecModel getWfSpec(String name, Integer version);

    <U extends Message, T extends AbstractGetable<U>> T get(ObjectIdModel<?, U, T> id);

    ServerContext context();

    List<String> adminPrincipalIds();

    TenantModel getTenant(String tenantId);

    /**
     * Gets a Principal for a specified ID. If the principalId is `null`, then
     * we fetch the anonymous principal
     *
     * Note that it is impossible to delete the `anonymous` principal: all we can
     * do is remove all of its permissions.
     * @param principalId
     * @return
     */
    PrincipalModel getPrincipal(String principalId);
}

package io.littlehorse.server.streams.lhinternalscan.publicrequests;

import com.google.protobuf.Message;
import io.littlehorse.common.LHStore;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.core.taskworkergroup.TaskWorkerGroupModel;
import io.littlehorse.common.model.getable.objectId.TaskDefIdModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.ListTaskWorkerGroupRequest;
import io.littlehorse.sdk.common.proto.ListTaskWorkerGroupResponse;
import io.littlehorse.sdk.common.proto.TaskWorkerGroup;
import io.littlehorse.server.streams.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streams.lhinternalscan.SearchScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.TagScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.ListTaskWorkerGroupReply;
import io.littlehorse.server.streams.storeinternals.index.Attribute;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.List;
import java.util.Optional;

public class ListTaskWorkerGroupModel
        extends PublicScanRequest<
                ListTaskWorkerGroupRequest,
                ListTaskWorkerGroupResponse,
                TaskWorkerGroup,
                TaskWorkerGroupModel,
                ListTaskWorkerGroupReply> {

    private TaskDefIdModel taskDefId;

    @Override
    public ListTaskWorkerGroupRequest.Builder toProto() {
        ListTaskWorkerGroupRequest.Builder out = ListTaskWorkerGroupRequest.newBuilder();
        if (taskDefId != null) {
            out.setTaskDefId(taskDefId.toProto());
        }
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeError {
        ListTaskWorkerGroupRequest p = (ListTaskWorkerGroupRequest) proto;
        if (p.getTaskWorkerGroupCriteriaCase() == ListTaskWorkerGroupRequest.TaskWorkerGroupCriteriaCase.TASK_DEF_ID) {
            taskDefId = TaskDefIdModel.fromProto(p.getTaskDefId(), TaskDefIdModel.class, context);
        }
    }

    @Override
    public Class<ListTaskWorkerGroupRequest> getProtoBaseClass() {
        return ListTaskWorkerGroupRequest.class;
    }

    @Override
    public GetableClassEnum getObjectType() {
        return GetableClassEnum.TASK_WORKER_GROUP;
    }

    @Override
    public LHStore getStoreType() {
        return LHStore.CORE;
    }

    @Override
    public TagStorageType indexTypeForSearch() throws LHApiException {
        return TagStorageType.LOCAL;
    }

    @Override
    public List<Attribute> getSearchAttributes() throws LHApiException {
        if (taskDefId != null) {
            return List.of(new Attribute("taskDefId", taskDefId.getName()));
        } else {
            return List.of();
        }
    }

    @Override
    public SearchScanBoundaryStrategy getScanBoundary(String searchAttributeString) throws LHApiException {
        return new TagScanBoundaryStrategy(searchAttributeString, Optional.empty(), Optional.empty());
    }
}

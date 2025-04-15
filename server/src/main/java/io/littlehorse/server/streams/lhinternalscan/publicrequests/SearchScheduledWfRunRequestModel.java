package io.littlehorse.server.streams.lhinternalscan.publicrequests;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHStore;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.objectId.ScheduledWfRunIdModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.ScheduledWfRunId;
import io.littlehorse.sdk.common.proto.ScheduledWfRunIdList;
import io.littlehorse.sdk.common.proto.SearchScheduledWfRunRequest;
import io.littlehorse.server.streams.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streams.lhinternalscan.SearchScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.TagScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.SearchScheduledWfRunReply;
import io.littlehorse.server.streams.storeinternals.index.Attribute;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SearchScheduledWfRunRequestModel
        extends PublicScanRequest<
                SearchScheduledWfRunRequest,
                ScheduledWfRunIdList,
                ScheduledWfRunId,
                ScheduledWfRunIdModel,
                SearchScheduledWfRunReply> {

    private String wfSpecName;
    private Integer majorVersion;
    private Integer revision;

    @Override
    public SearchScheduledWfRunRequest.Builder toProto() {
        SearchScheduledWfRunRequest.Builder out =
                SearchScheduledWfRunRequest.newBuilder().setWfSpecName(wfSpecName);
        if (majorVersion != null) {
            out.setMajorVersion(majorVersion);
        }
        if (revision != null) {
            out.setRevision(revision);
        }
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        SearchScheduledWfRunRequest p = (SearchScheduledWfRunRequest) proto;
        wfSpecName = p.getWfSpecName();
        if (p.hasMajorVersion()) {
            majorVersion = p.getMajorVersion();
        }
        if (p.hasRevision()) {
            revision = p.getRevision();
        }
    }

    @Override
    public Class<SearchScheduledWfRunRequest> getProtoBaseClass() {
        return SearchScheduledWfRunRequest.class;
    }

    @Override
    public GetableClassEnum getObjectType() {
        return GetableClassEnum.SCHEDULED_WF_RUN;
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
    public SearchScanBoundaryStrategy getScanBoundary(String searchAttributeString) throws LHApiException {
        return new TagScanBoundaryStrategy(searchAttributeString, Optional.empty(), Optional.empty());
    }

    @Override
    public List<Attribute> getSearchAttributes() throws LHApiException {
        List<Attribute> out = new ArrayList<>();

        if (majorVersion != null) {
            if (revision == null) {
                log.info("query for: " + wfSpecName + "/" + LHUtil.toLHDbVersionFormat(majorVersion));
                out.add(new Attribute("majorVersion", wfSpecName + "/" + LHUtil.toLHDbVersionFormat(majorVersion)));
            } else {
                out.add(new Attribute("wfSpecId", new WfSpecIdModel(wfSpecName, majorVersion, revision).toString()));
            }
        } else {
            if (revision != null) {
                throw new LHApiException(
                        Status.INVALID_ARGUMENT, "Cannot provide wfSpecRevision without wfSpecMajorVersion");
            }
            out.add(new Attribute("wfSpecName", wfSpecName));
        }
        return out;
    }
}

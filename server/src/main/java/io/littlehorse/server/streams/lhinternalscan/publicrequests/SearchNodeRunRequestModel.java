package io.littlehorse.server.streams.lhinternalscan.publicrequests;

import com.google.protobuf.Message;
import com.google.protobuf.Timestamp;
import io.littlehorse.common.LHStore;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.objectId.ExternalEventDefIdModel;
import io.littlehorse.common.model.getable.objectId.NodeRunIdModel;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.NodeRunId;
import io.littlehorse.sdk.common.proto.NodeRunIdList;
import io.littlehorse.sdk.common.proto.SearchNodeRunRequest;
import io.littlehorse.server.streams.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streams.lhinternalscan.SearchScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.TagScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.SearchNodeRunReply;
import io.littlehorse.server.streams.storeinternals.index.Attribute;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class SearchNodeRunRequestModel
        extends PublicScanRequest<SearchNodeRunRequest, NodeRunIdList, NodeRunId, NodeRunIdModel, SearchNodeRunReply> {

    private Timestamp earliestStart;
    private Timestamp latestStart;
    private ExternalEventDefIdModel externalEventDefId;

    public GetableClassEnum getObjectType() {
        return GetableClassEnum.NODE_RUN;
    }

    public Class<SearchNodeRunRequest> getProtoBaseClass() {
        return SearchNodeRunRequest.class;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        SearchNodeRunRequest p = (SearchNodeRunRequest) proto;
        if (p.hasLimit()) limit = p.getLimit();
        if (p.hasBookmark()) {
            try {
                bookmark = BookmarkPb.parseFrom(p.getBookmark());
            } catch (Exception exn) {
                log.error("Failed to load bookmark: {}", exn.getMessage(), exn);
            }
        }

        if (p.hasEarliestStart()) earliestStart = p.getEarliestStart();
        if (p.hasLatestStart()) latestStart = p.getLatestStart();

        if (p.hasExternalEventDef()) {
            externalEventDefId =
                    ExternalEventDefIdModel.fromProto(p.getExternalEventDef(), ExternalEventDefIdModel.class, context);
        }
    }

    public SearchNodeRunRequest.Builder toProto() {
        SearchNodeRunRequest.Builder out = SearchNodeRunRequest.newBuilder();
        if (bookmark != null) {
            out.setBookmark(bookmark.toByteString());
        }
        if (limit != null) {
            out.setLimit(limit);
        }
        if (externalEventDefId != null) {
            out.setExternalEventDef(externalEventDefId.toProto());
        }

        if (earliestStart != null) out.setEarliestStart(earliestStart);
        if (latestStart != null) out.setLatestStart(latestStart);
        return out;
    }

    public static SearchNodeRunRequestModel fromProto(SearchNodeRunRequest proto, ExecutionContext context) {
        SearchNodeRunRequestModel out = new SearchNodeRunRequestModel();
        out.initFrom(proto, context);
        return out;
    }

    @Override
    public TagStorageType indexTypeForSearch() throws LHApiException {
        return TagStorageType.LOCAL;
    }

    @Override
    public LHStore getStoreType() {
        return LHStore.CORE; // only object id prefix scan supported.
    }

    @Override
    public SearchScanBoundaryStrategy getScanBoundary(String searchAttributeString) throws LHApiException {
        Optional<Date> earliest =
                earliestStart == null ? Optional.empty() : Optional.of(LHUtil.fromProtoTs(earliestStart));
        Optional<Date> latest = latestStart == null ? Optional.empty() : Optional.of(LHUtil.fromProtoTs(latestStart));
        return new TagScanBoundaryStrategy(searchAttributeString, earliest, latest);
    }

    @Override
    public List<Attribute> getSearchAttributes() {
        return List.of(new Attribute("extEvtDefName", externalEventDefId.toString()));
    }
}

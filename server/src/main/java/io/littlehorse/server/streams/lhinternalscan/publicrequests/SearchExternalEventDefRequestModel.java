package io.littlehorse.server.streams.lhinternalscan.publicrequests;

import com.google.protobuf.Message;
import io.littlehorse.common.model.getable.objectId.ExternalEventDefIdModel;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.LHStoreType;
import io.littlehorse.sdk.common.proto.ExternalEventDefId;
import io.littlehorse.sdk.common.proto.ExternalEventDefIdList;
import io.littlehorse.sdk.common.proto.SearchExternalEventDefRequest;
import io.littlehorse.server.streams.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.SearchExternalEventDefReply;
import io.littlehorse.server.streams.lhinternalscan.util.BoundedObjectIdScanModel;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.RequestExecutionContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SearchExternalEventDefRequestModel
        extends PublicScanRequest<
                SearchExternalEventDefRequest,
                ExternalEventDefIdList,
                ExternalEventDefId,
                ExternalEventDefIdModel,
                SearchExternalEventDefReply> {

    public String prefix;

    @Override
    public Class<SearchExternalEventDefRequest> getProtoBaseClass() {
        return SearchExternalEventDefRequest.class;
    }

    @Override
    public LHStoreType getStoreType() {
        return LHStoreType.METADATA;
    }

    public GetableClassEnum getObjectType() {
        return GetableClassEnum.EXTERNAL_EVENT_DEF;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        SearchExternalEventDefRequest p = (SearchExternalEventDefRequest) proto;
        if (p.hasLimit()) limit = p.getLimit();
        if (p.hasBookmark()) {
            try {
                bookmark = BookmarkPb.parseFrom(p.getBookmark());
            } catch (Exception exn) {
                log.error("Failed to load bookmark: {}", exn.getMessage(), exn);
            }
        }
        if (p.hasPrefix()) prefix = p.getPrefix();
    }

    @Override
    public SearchExternalEventDefRequest.Builder toProto() {
        SearchExternalEventDefRequest.Builder out = SearchExternalEventDefRequest.newBuilder();
        if (bookmark != null) {
            out.setBookmark(bookmark.toByteString());
        }
        if (limit != null) {
            out.setLimit(limit);
        }
        if (prefix != null) out.setPrefix(prefix);

        return out;
    }

    public static SearchExternalEventDefRequestModel fromProto(
            SearchExternalEventDefRequest proto, ExecutionContext context) {
        SearchExternalEventDefRequestModel out = new SearchExternalEventDefRequestModel();
        out.initFrom(proto, context);
        return out;
    }

    @Override
    public BoundedObjectIdScanModel<ExternalEventDefIdModel> getScanBoundary(RequestExecutionContext ctx) {
        String scanPrefix = prefix != null ? prefix : "";
        return new BoundedObjectIdScanModel<>(getObjectType(), scanPrefix);
    }
}

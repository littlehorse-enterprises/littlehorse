package io.littlehorse.server.streams.lhinternalscan.publicrequests;

import com.google.protobuf.Message;
import io.littlehorse.common.model.getable.objectId.TaskDefIdModel;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.LHStoreType;
import io.littlehorse.sdk.common.proto.SearchTaskDefRequest;
import io.littlehorse.sdk.common.proto.TaskDefId;
import io.littlehorse.sdk.common.proto.TaskDefIdList;
import io.littlehorse.server.streams.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.SearchTaskDefReply;
import io.littlehorse.server.streams.lhinternalscan.util.BoundedObjectIdScanModel;
import io.littlehorse.server.streams.lhinternalscan.util.ScanBoundary;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.RequestExecutionContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SearchTaskDefRequestModel
        extends PublicScanRequest<SearchTaskDefRequest, TaskDefIdList, TaskDefId, TaskDefIdModel, SearchTaskDefReply> {

    public String prefix;

    public Class<SearchTaskDefRequest> getProtoBaseClass() {
        return SearchTaskDefRequest.class;
    }

    @Override
    public LHStoreType getStoreType() {
        return LHStoreType.METADATA;
    }

    @Override
    public GetableClassEnum getObjectType() {
        return GetableClassEnum.TASK_DEF;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        SearchTaskDefRequest p = (SearchTaskDefRequest) proto;
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
    public SearchTaskDefRequest.Builder toProto() {
        SearchTaskDefRequest.Builder out = SearchTaskDefRequest.newBuilder();
        if (bookmark != null) {
            out.setBookmark(bookmark.toByteString());
        }
        if (limit != null) {
            out.setLimit(limit);
        }
        if (prefix != null) out.setPrefix(prefix);

        return out;
    }

    public static SearchTaskDefRequestModel fromProto(SearchTaskDefRequest proto, ExecutionContext context) {
        SearchTaskDefRequestModel out = new SearchTaskDefRequestModel();
        out.initFrom(proto, context);
        return out;
    }

    @Override
    public ScanBoundary<?> getScanBoundary(RequestExecutionContext ctx) {
        String scanPrefix = prefix != null ? prefix : "";
        return new BoundedObjectIdScanModel(GetableClassEnum.TASK_DEF, scanPrefix);
    }
}

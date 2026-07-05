package io.littlehorse.server.streams.lhinternalscan.publicrequests;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import io.littlehorse.common.LHStore;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.objectId.BulkJobIdModel;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.BulkJobId;
import io.littlehorse.sdk.common.proto.BulkJobIdList;
import io.littlehorse.sdk.common.proto.BulkJobStatus;
import io.littlehorse.sdk.common.proto.SearchBulkJobRequest;
import io.littlehorse.server.streams.lhinternalscan.ObjectIdScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streams.lhinternalscan.SearchScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.TagScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.SearchBulkJobReply;
import io.littlehorse.server.streams.storeinternals.index.Attribute;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SearchBulkJobRequestModel
        extends PublicScanRequest<SearchBulkJobRequest, BulkJobIdList, BulkJobId, BulkJobIdModel, SearchBulkJobReply> {

    private BulkJobStatus status;

    @Override
    public GetableClassEnum getObjectType() {
        return GetableClassEnum.BULK_JOB;
    }

    @Override
    public LHStore getStoreType() {
        return LHStore.GLOBAL_METADATA;
    }

    @Override
    public TagStorageType indexTypeForSearch() throws LHApiException {
        return TagStorageType.LOCAL;
    }

    @Override
    public SearchScanBoundaryStrategy getScanBoundary(String searchAttributeString) throws LHApiException {
        if (status == null) {
            return ObjectIdScanBoundaryStrategy.prefixMetadataScan();
        }
        return new TagScanBoundaryStrategy(searchAttributeString, Optional.empty(), Optional.empty());
    }

    @Override
    public List<Attribute> getSearchAttributes() throws LHApiException {
        if (status != null) {
            return List.of(new Attribute("status", status.toString()));
        }
        return List.of();
    }

    @Override
    public Class<SearchBulkJobRequest> getProtoBaseClass() {
        return SearchBulkJobRequest.class;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        SearchBulkJobRequest p = (SearchBulkJobRequest) proto;
        if (p.hasLimit()) limit = p.getLimit();
        if (p.hasBookmark()) {
            try {
                bookmark = BookmarkPb.parseFrom(p.getBookmark());
            } catch (InvalidProtocolBufferException exn) {
                throw new LHSerdeException(String.format("Failed to load bookmark: %s", exn.getMessage()));
            }
        }
        if (p.hasStatus()) status = p.getStatus();
    }

    @Override
    public SearchBulkJobRequest.Builder toProto() {
        SearchBulkJobRequest.Builder out = SearchBulkJobRequest.newBuilder();
        if (bookmark != null) out.setBookmark(bookmark.toByteString());
        if (limit != null) out.setLimit(limit);
        if (status != null) out.setStatus(status);
        return out;
    }

    public static SearchBulkJobRequestModel fromProto(SearchBulkJobRequest proto, ExecutionContext context) {
        SearchBulkJobRequestModel out = new SearchBulkJobRequestModel();
        out.initFrom(proto, context);
        return out;
    }
}


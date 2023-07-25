package io.littlehorse.server.streamsimpl.lhinternalscan.publicrequests;

import com.google.common.base.Strings;
import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.objectId.WfSpecId;
import io.littlehorse.common.proto.AttributePb;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GetableClassEnumPb;
import io.littlehorse.common.proto.InternalScanPb;
import io.littlehorse.common.proto.InternalScanPb.BoundedObjectIdScanPb;
import io.littlehorse.common.proto.InternalScanPb.ScanBoundaryCase;
import io.littlehorse.common.proto.ScanResultTypePb;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.sdk.common.proto.SearchWfSpecPb;
import io.littlehorse.sdk.common.proto.SearchWfSpecPb.WfSpecCriteriaCase;
import io.littlehorse.sdk.common.proto.SearchWfSpecReplyPb;
import io.littlehorse.sdk.common.proto.WfSpecIdPb;
import io.littlehorse.server.streamsimpl.ServerTopology;
import io.littlehorse.server.streamsimpl.lhinternalscan.InternalScan;
import io.littlehorse.server.streamsimpl.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies.SearchWfSpecReply;
import io.littlehorse.server.streamsimpl.storeinternals.index.Attribute;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
public class SearchWfSpec
    extends PublicScanRequest<SearchWfSpecPb, SearchWfSpecReplyPb, WfSpecIdPb, WfSpecId, SearchWfSpecReply> {

    private WfSpecCriteriaCase type;
    private String name;
    private String taskDefName;
    private String prefix;

    public Class<SearchWfSpecPb> getProtoBaseClass() {
        return SearchWfSpecPb.class;
    }

    public GetableClassEnumPb getObjectType() {
        return GetableClassEnumPb.WF_SPEC;
    }

    public void initFrom(Message proto) {
        SearchWfSpecPb p = (SearchWfSpecPb) proto;
        if (p.hasLimit()) limit = p.getLimit();
        if (p.hasBookmark()) {
            try {
                bookmark = BookmarkPb.parseFrom(p.getBookmark());
            } catch (Exception exn) {
                log.error("Failed to load bookmark: {}", exn.getMessage(), exn);
            }
        }

        type = p.getWfSpecCriteriaCase();
        switch (type) {
            case NAME:
                name = p.getName();
                break;
            case PREFIX:
                prefix = p.getPrefix();
                break;
            case TASK_DEF_NAME:
                taskDefName = p.getTaskDefName();
                break;
            case WFSPECCRITERIA_NOT_SET:
            // nothing to do, we just return all the WfSpec's.
        }
    }

    public SearchWfSpecPb.Builder toProto() {
        SearchWfSpecPb.Builder out = SearchWfSpecPb.newBuilder();
        if (bookmark != null) {
            out.setBookmark(bookmark.toByteString());
        }
        if (limit != null) {
            out.setLimit(limit);
        }
        switch (type) {
            case NAME:
                out.setName(name);
                break;
            case PREFIX:
                out.setPrefix(prefix);
                break;
            case TASK_DEF_NAME:
                out.setTaskDefName(taskDefName);
                break;
            case WFSPECCRITERIA_NOT_SET:
            // nothing to do, we just return all the WfSpec's.
        }
        return out;
    }

    public static SearchWfSpec fromProto(SearchWfSpecPb proto) {
        SearchWfSpec out = new SearchWfSpec();
        out.initFrom(proto);
        return out;
    }

    public InternalScan startInternalSearch(LHGlobalMetaStores stores)
        throws LHValidationError {
        InternalScan out = new InternalScan();
        out.storeName = ServerTopology.CORE_STORE;
        out.resultType = ScanResultTypePb.OBJECT_ID;
        out.type = ScanBoundaryCase.BOUNDED_OBJECT_ID_SCAN;
        out.partitionKey = LHConstants.META_PARTITION_KEY;

        if (name != null && !name.equals("")) {
            // exact match on name
            out.boundedObjectIdScan =
                BoundedObjectIdScanPb
                    .newBuilder()
                    .setStartObjectId(name + "/")
                    .setEndObjectId(name + "/~")
                    .build();
        } else if (prefix != null && !prefix.equals("")) {
            // Prefix scan on name
            out.boundedObjectIdScan =
                BoundedObjectIdScanPb
                    .newBuilder()
                    .setStartObjectId(prefix)
                    .setEndObjectId(prefix + "~")
                    .build();
        } else if (!Strings.isNullOrEmpty(taskDefName)) {
            out.partitionKey = null;
            out.type = ScanBoundaryCase.TAG_SCAN;
            out.tagScan =
                InternalScanPb.TagScanPb
                    .newBuilder()
                    .setKeyPrefix(tagPrefixStoreKey())
                    .build();
        } else {
            // that means we want to search all wfSpecs
            out.boundedObjectIdScan =
                BoundedObjectIdScanPb.newBuilder().setStartObjectId("").build();
        }

        return out;
    }

    @Override
    public List<Attribute> searchAttributes() throws LHValidationError {
        return List.of(new Attribute("taskDef", taskDefName));
    }
}

package io.littlehorse.server.streamsimpl.lhinternalscan.publicrequests;

import com.google.common.base.Strings;
import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.Getable;
import io.littlehorse.common.model.meta.WfSpecModel;
import io.littlehorse.common.model.objectId.WfSpecId;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GetableClassEnumPb;
import io.littlehorse.common.proto.TagStorageTypePb;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.sdk.common.proto.SearchWfSpecPb;
import io.littlehorse.sdk.common.proto.SearchWfSpecPb.WfSpecCriteriaCase;
import io.littlehorse.sdk.common.proto.SearchWfSpecReplyPb;
import io.littlehorse.sdk.common.proto.WfSpecIdPb;
import io.littlehorse.server.streamsimpl.lhinternalscan.ObjectIdScanBoundaryStrategy;
import io.littlehorse.server.streamsimpl.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streamsimpl.lhinternalscan.SearchScanBoundaryStrategy;
import io.littlehorse.server.streamsimpl.lhinternalscan.TagScanBoundaryStrategy;
import io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies.SearchWfSpecReply;
import io.littlehorse.server.streamsimpl.storeinternals.GetableIndex;
import io.littlehorse.server.streamsimpl.storeinternals.index.Attribute;
import java.util.List;
import java.util.Optional;
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

    @Override
    public List<Attribute> getSearchAttributes() {
        return List.of(new Attribute("taskDef", taskDefName));
    }

    @Override
    public TagStorageTypePb indexTypeForSearch(LHGlobalMetaStores stores)
        throws LHValidationError {
        if (taskDefName != null) {
            List<String> attributes = getSearchAttributes()
                .stream()
                .map(Attribute::getEscapedKey)
                .toList();
            for (GetableIndex<? extends Getable<?>> indexConfiguration : new WfSpecModel()
                .getIndexConfigurations()) {
                if (
                    indexConfiguration.searchAttributesMatch(attributes) &&
                    indexConfiguration.getTagStorageTypePb().isPresent()
                ) {
                    return indexConfiguration.getTagStorageTypePb().get();
                }
            }
            return null;
        } else {
            return TagStorageTypePb.LOCAL;
        }
    }

    @Override
    public void validate() throws LHValidationError {}

    @Override
    public SearchScanBoundaryStrategy getScanBoundary(String searchAttributeString) {
        if (name != null && !name.equals("")) {
            return new ObjectIdScanBoundaryStrategy(
                LHConstants.META_PARTITION_KEY,
                name + "/",
                name + "/~"
            );
        } else if (prefix != null && !prefix.isEmpty()) {
            return new ObjectIdScanBoundaryStrategy(
                LHConstants.META_PARTITION_KEY,
                prefix,
                prefix + "~"
            );
        } else if (!Strings.isNullOrEmpty(taskDefName)) {
            return new TagScanBoundaryStrategy(
                searchAttributeString,
                Optional.empty(),
                Optional.empty()
            );
        } else {
            return new ObjectIdScanBoundaryStrategy(
                LHConstants.META_PARTITION_KEY,
                "",
                "~"
            );
        }
    }
}

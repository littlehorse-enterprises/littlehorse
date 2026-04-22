package io.littlehorse.server.streams.lhinternalscan.publicrequests;

import com.google.common.base.Strings;
import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHStore;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.sdk.common.proto.SearchWfSpecRequest;
import io.littlehorse.sdk.common.proto.SearchWfSpecRequest.WfSpecCriteriaCase;
import io.littlehorse.sdk.common.proto.WfSpecId;
import io.littlehorse.sdk.common.proto.WfSpecIdList;
import io.littlehorse.server.streams.lhinternalscan.ObjectIdScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streams.lhinternalscan.SearchScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.TagScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.SearchWfSpecReply;
import io.littlehorse.server.streams.storeinternals.GetableIndex;
import io.littlehorse.server.streams.storeinternals.index.Attribute;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.List;
import java.util.Optional;

public class SearchWfSpecRequestModel
        extends PublicScanRequest<SearchWfSpecRequest, WfSpecIdList, WfSpecId, WfSpecIdModel, SearchWfSpecReply> {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SearchWfSpecRequestModel.class);
    private WfSpecCriteriaCase type;
    private String name;
    private String taskDefName;
    private String prefix;
    private ExecutionContext executionContext;

    public Class<SearchWfSpecRequest> getProtoBaseClass() {
        return SearchWfSpecRequest.class;
    }

    public GetableClassEnum getObjectType() {
        return GetableClassEnum.WF_SPEC;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        SearchWfSpecRequest p = (SearchWfSpecRequest) proto;
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
        }
        // nothing to do, we just return all the WfSpec's.
        this.executionContext = context;
    }

    public SearchWfSpecRequest.Builder toProto() {
        SearchWfSpecRequest.Builder out = SearchWfSpecRequest.newBuilder();
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
        }
        // nothing to do, we just return all the WfSpec's.
        return out;
    }

    public static SearchWfSpecRequestModel fromProto(SearchWfSpecRequest proto, ExecutionContext context) {
        SearchWfSpecRequestModel out = new SearchWfSpecRequestModel();
        out.initFrom(proto, context);
        return out;
    }

    @Override
    public List<Attribute> getSearchAttributes() {
        return List.of(new Attribute("taskDef", taskDefName));
    }

    @Override
    public TagStorageType indexTypeForSearch() throws LHApiException {
        if (taskDefName != null) {
            List<String> attributes =
                    getSearchAttributes().stream().map(Attribute::getEscapedKey).toList();
            for (GetableIndex<? extends AbstractGetable<?>> indexConfiguration :
                    new WfSpecModel().getIndexConfigurations()) {
                if (indexConfiguration.searchAttributesMatch(attributes)
                        && indexConfiguration.getTagStorageType().isPresent()) {
                    return indexConfiguration.getTagStorageType().get();
                }
            }
            return null;
        } else {
            return TagStorageType.LOCAL;
        }
    }

    @Override
    public LHStore getStoreType() {
        return LHStore.GLOBAL_METADATA;
    }

    @Override
    public SearchScanBoundaryStrategy getScanBoundary(String searchAttributeString) {
        if (name != null && !name.equals("")) {
            return new ObjectIdScanBoundaryStrategy(LHConstants.META_PARTITION_KEY, name + "/", name + "/~");
        } else if (prefix != null && !prefix.isEmpty()) {
            return ObjectIdScanBoundaryStrategy.metadataSearchFor(prefix);
        } else if (!Strings.isNullOrEmpty(taskDefName)) {
            return new TagScanBoundaryStrategy(searchAttributeString, Optional.empty(), Optional.empty());
        } else {
            return ObjectIdScanBoundaryStrategy.prefixMetadataScan();
        }
    }

    public WfSpecCriteriaCase getType() {
        return this.type;
    }

    public String getName() {
        return this.name;
    }

    public String getTaskDefName() {
        return this.taskDefName;
    }

    public String getPrefix() {
        return this.prefix;
    }

    public ExecutionContext getExecutionContext() {
        return this.executionContext;
    }

    public void setType(final WfSpecCriteriaCase type) {
        this.type = type;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setTaskDefName(final String taskDefName) {
        this.taskDefName = taskDefName;
    }

    public void setPrefix(final String prefix) {
        this.prefix = prefix;
    }

    public void setExecutionContext(final ExecutionContext executionContext) {
        this.executionContext = executionContext;
    }
}

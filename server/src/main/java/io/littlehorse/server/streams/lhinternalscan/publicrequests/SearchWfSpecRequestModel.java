package io.littlehorse.server.streams.lhinternalscan.publicrequests;

import com.google.common.base.Strings;
import com.google.protobuf.Message;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.LHStoreType;
import io.littlehorse.sdk.common.proto.SearchWfSpecRequest;
import io.littlehorse.sdk.common.proto.SearchWfSpecRequest.WfSpecCriteriaCase;
import io.littlehorse.sdk.common.proto.WfSpecId;
import io.littlehorse.sdk.common.proto.WfSpecIdList;
import io.littlehorse.server.streams.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.SearchWfSpecReply;
import io.littlehorse.server.streams.lhinternalscan.util.BoundedObjectIdScanModel;
import io.littlehorse.server.streams.lhinternalscan.util.ScanBoundary;
import io.littlehorse.server.streams.lhinternalscan.util.TagScanModel;
import io.littlehorse.server.streams.storeinternals.index.Attribute;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.RequestExecutionContext;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
public class SearchWfSpecRequestModel
        extends PublicScanRequest<SearchWfSpecRequest, WfSpecIdList, WfSpecId, WfSpecIdModel, SearchWfSpecReply> {

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
                // nothing to do, we just return all the WfSpec's.
        }
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
                // nothing to do, we just return all the WfSpec's.
        }
        return out;
    }

    public static SearchWfSpecRequestModel fromProto(SearchWfSpecRequest proto, ExecutionContext context) {
        SearchWfSpecRequestModel out = new SearchWfSpecRequestModel();
        out.initFrom(proto, context);
        return out;
    }

    @Override
    public LHStoreType getStoreType() {
        return LHStoreType.METADATA;
    }

    @Override
    public ScanBoundary<?, WfSpecIdModel> getScanBoundary(RequestExecutionContext ctx) {
        if (name != null && !name.equals("")) {
            return new BoundedObjectIdScanModel<>(GetableClassEnum.WF_SPEC, name + "/");
        } else if (prefix != null && !prefix.isEmpty()) {
            return new BoundedObjectIdScanModel<>(GetableClassEnum.WF_SPEC, prefix);
        } else if (!Strings.isNullOrEmpty(taskDefName)) {
            return new TagScanModel<WfSpecIdModel>(GetableClassEnum.WF_SPEC).add(new Attribute("taskDef", taskDefName));
        } else {
            return new BoundedObjectIdScanModel<>(GetableClassEnum.WF_SPEC, "");
        }
    }
}

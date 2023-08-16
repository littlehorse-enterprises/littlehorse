package io.littlehorse.server.streamsimpl.lhinternalscan.publicrequests;

import com.google.protobuf.Message;
import com.google.protobuf.Timestamp;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.objectId.WfRunId;
import io.littlehorse.common.model.wfrun.WfRunModel;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GetableClassEnumPb;
import io.littlehorse.common.proto.TagStorageTypePb;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.SearchWfRunPb;
import io.littlehorse.sdk.common.proto.SearchWfRunPb.NamePb;
import io.littlehorse.sdk.common.proto.SearchWfRunPb.StatusAndNamePb;
import io.littlehorse.sdk.common.proto.SearchWfRunPb.StatusAndSpecPb;
import io.littlehorse.sdk.common.proto.SearchWfRunPb.WfrunCriteriaCase;
import io.littlehorse.sdk.common.proto.SearchWfRunReplyPb;
import io.littlehorse.sdk.common.proto.WfRunIdPb;
import io.littlehorse.server.streamsimpl.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streamsimpl.lhinternalscan.SearchScanBoundaryStrategy;
import io.littlehorse.server.streamsimpl.lhinternalscan.TagScanBoundaryStrategy;
import io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies.SearchWfRunReply;
import io.littlehorse.server.streamsimpl.storeinternals.GetableIndex;
import io.littlehorse.server.streamsimpl.storeinternals.index.Attribute;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SearchWfRun
    extends PublicScanRequest<SearchWfRunPb, SearchWfRunReplyPb, WfRunIdPb, WfRunId, SearchWfRunReply> {

    public WfrunCriteriaCase type;
    public StatusAndSpecPb statusAndSpec;
    private NamePb namePb;
    private StatusAndNamePb statusAndName;

    public GetableClassEnumPb getObjectType() {
        return GetableClassEnumPb.WF_RUN;
    }

    public Class<SearchWfRunPb> getProtoBaseClass() {
        return SearchWfRunPb.class;
    }

    public void initFrom(Message proto) {
        SearchWfRunPb p = (SearchWfRunPb) proto;
        if (p.hasLimit()) limit = p.getLimit();
        if (p.hasBookmark()) {
            try {
                bookmark = BookmarkPb.parseFrom(p.getBookmark());
            } catch (Exception exn) {
                log.error("Failed to load bookmark: {}", exn.getMessage(), exn);
            }
        }

        type = p.getWfrunCriteriaCase();
        switch (type) {
            case STATUS_AND_SPEC:
                statusAndSpec = p.getStatusAndSpec();
                break;
            case NAME:
                namePb = p.getName();
                break;
            case STATUS_AND_NAME:
                statusAndName = p.getStatusAndName();
                break;
            case WFRUNCRITERIA_NOT_SET:
                throw new RuntimeException("Not possible");
        }
    }

    public SearchWfRunPb.Builder toProto() {
        SearchWfRunPb.Builder out = SearchWfRunPb.newBuilder();
        if (bookmark != null) {
            out.setBookmark(bookmark.toByteString());
        }
        if (limit != null) {
            out.setLimit(limit);
        }
        switch (type) {
            case STATUS_AND_SPEC:
                out.setStatusAndSpec(statusAndSpec);
                break;
            case NAME:
                out.setName(namePb);
                break;
            case STATUS_AND_NAME:
                out.setStatusAndName(statusAndName);
                break;
            case WFRUNCRITERIA_NOT_SET:
                throw new RuntimeException("not possible");
        }

        return out;
    }

    public static SearchWfRun fromProto(SearchWfRunPb proto) {
        SearchWfRun out = new SearchWfRun();
        out.initFrom(proto);
        return out;
    }

    private List<WfrunCriteriaCase> supportedCriteriaCases() {
        return List.of(
            WfrunCriteriaCase.STATUS_AND_SPEC,
            WfrunCriteriaCase.NAME,
            WfrunCriteriaCase.STATUS_AND_NAME
        );
    }

    private Timestamp getEarliestStart() {
        if (type == WfrunCriteriaCase.STATUS_AND_SPEC) {
            if (statusAndSpec.hasEarliestStart()) {
                return statusAndSpec.getEarliestStart();
            }
        } else if (type == WfrunCriteriaCase.STATUS_AND_NAME) {
            if (statusAndName.hasEarliestStart()) {
                return statusAndName.getEarliestStart();
            }
        }
        return null;
    }

    private Timestamp getLatestStart() {
        if (type == WfrunCriteriaCase.STATUS_AND_SPEC) {
            if (statusAndSpec.hasLatestStart()) {
                return statusAndSpec.getLatestStart();
            }
        } else if (type == WfrunCriteriaCase.STATUS_AND_NAME) {
            if (statusAndName.hasLatestStart()) {
                return statusAndName.getLatestStart();
            }
        }
        return null;
    }

    public List<Attribute> getSearchAttributes() {
        if (type == WfrunCriteriaCase.STATUS_AND_SPEC) {
            return buildStatusAndSpecAttributesPb();
        } else if (type == WfrunCriteriaCase.NAME) {
            return buildNameAttributePb();
        } else {
            return buildStatusAndNameAttributesPb();
        }
    }

    @Override
    public TagStorageTypePb indexTypeForSearch(LHGlobalMetaStores stores)
        throws LHValidationError {
        List<String> searchAttributeKeys = getSearchAttributes()
            .stream()
            .map(Attribute::getEscapedKey)
            .toList();
        return new WfRunModel()
            .getIndexConfigurations()
            .stream()
            .filter(getableIndexConfiguration ->
                getableIndexConfiguration.searchAttributesMatch(searchAttributeKeys)
            )
            .map(GetableIndex::getTagStorageTypePb)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst()
            .orElse(null);
    }

    @Override
    public void validate() throws LHValidationError {
        if (!supportedCriteriaCases().contains(type)) {
            throw new LHValidationError("Search case not supported yet");
        }
    }

    @Override
    public SearchScanBoundaryStrategy getScanBoundary(String searchAttributeString) {
        return new TagScanBoundaryStrategy(
            searchAttributeString,
            Optional.ofNullable(LHUtil.fromProtoTs(getEarliestStart())),
            Optional.ofNullable(LHUtil.fromProtoTs(getLatestStart()))
        );
    }

    private List<Attribute> buildStatusAndNameAttributesPb() {
        return Arrays.asList(
            new Attribute("wfSpecName", statusAndName.getWfSpecName()),
            new Attribute("status", statusAndName.getStatus().toString())
        );
    }

    private List<Attribute> buildNameAttributePb() {
        return List.of(new Attribute("wfSpecName", namePb.getWfSpecName()));
    }

    private List<Attribute> buildStatusAndSpecAttributesPb() {
        return Arrays.asList(
            new Attribute("wfSpecName", statusAndSpec.getWfSpecName()),
            new Attribute("status", statusAndSpec.getStatus().toString()),
            new Attribute(
                "wfSpecVersion",
                LHUtil.toLHDbVersionFormat(statusAndSpec.getWfSpecVersion())
            )
        );
    }
}

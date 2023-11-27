package io.littlehorse.server.streams.lhinternalscan.publicrequests;

import com.google.protobuf.Message;
import com.google.protobuf.Timestamp;
import io.grpc.Status;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHStore;
import io.littlehorse.common.dao.ReadOnlyMetadataDAO;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.SearchWfRunRequest;
import io.littlehorse.sdk.common.proto.SearchWfRunRequest.NameRequest;
import io.littlehorse.sdk.common.proto.SearchWfRunRequest.StatusAndNameRequest;
import io.littlehorse.sdk.common.proto.SearchWfRunRequest.StatusAndSpecRequest;
import io.littlehorse.sdk.common.proto.SearchWfRunRequest.WfrunCriteriaCase;
import io.littlehorse.sdk.common.proto.WfRunId;
import io.littlehorse.sdk.common.proto.WfRunIdList;
import io.littlehorse.server.streams.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streams.lhinternalscan.SearchScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.TagScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.SearchWfRunReply;
import io.littlehorse.server.streams.storeinternals.GetableIndex;
import io.littlehorse.server.streams.storeinternals.index.Attribute;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SearchWfRunRequestModel
        extends PublicScanRequest<SearchWfRunRequest, WfRunIdList, WfRunId, WfRunIdModel, SearchWfRunReply> {

    public WfrunCriteriaCase type;
    public StatusAndSpecRequest statusAndSpec;
    private NameRequest namePb;
    private StatusAndNameRequest statusAndName;

    public GetableClassEnum getObjectType() {
        return GetableClassEnum.WF_RUN;
    }

    public Class<SearchWfRunRequest> getProtoBaseClass() {
        return SearchWfRunRequest.class;
    }

    public void initFrom(Message proto) {
        SearchWfRunRequest p = (SearchWfRunRequest) proto;
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

    public SearchWfRunRequest.Builder toProto() {
        SearchWfRunRequest.Builder out = SearchWfRunRequest.newBuilder();
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

    public static SearchWfRunRequestModel fromProto(SearchWfRunRequest proto) {
        SearchWfRunRequestModel out = new SearchWfRunRequestModel();
        out.initFrom(proto);
        return out;
    }

    private List<WfrunCriteriaCase> supportedCriteriaCases() {
        return List.of(WfrunCriteriaCase.STATUS_AND_SPEC, WfrunCriteriaCase.NAME, WfrunCriteriaCase.STATUS_AND_NAME);
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
    public TagStorageType indexTypeForSearch(ReadOnlyMetadataDAO readOnlyDao) throws LHApiException {
        List<String> searchAttributeKeys =
                getSearchAttributes().stream().map(Attribute::getEscapedKey).toList();
        return new WfRunModel()
                .getIndexConfigurations().stream()
                        .filter(getableIndexConfiguration ->
                                getableIndexConfiguration.searchAttributesMatch(searchAttributeKeys))
                        .map(GetableIndex::getTagStorageType)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .findFirst()
                        .orElse(null);
    }

    @Override
    public LHStore getStoreType() {
        return indexTypeForSearch(null) == TagStorageType.LOCAL ? LHStore.CORE : LHStore.REPARTITION;
    }

    @Override
    public SearchScanBoundaryStrategy getScanBoundary(String searchAttributeString) {
        if (!supportedCriteriaCases().contains(type)) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "Search case not supported yet");
        }
        return new TagScanBoundaryStrategy(
                searchAttributeString,
                Optional.ofNullable(LHUtil.fromProtoTs(getEarliestStart())),
                Optional.ofNullable(LHUtil.fromProtoTs(getLatestStart())));
    }

    private List<Attribute> buildStatusAndNameAttributesPb() {
        return Arrays.asList(
                new Attribute("wfSpecName", statusAndName.getWfSpecName()),
                new Attribute("status", statusAndName.getStatus().toString()));
    }

    private List<Attribute> buildNameAttributePb() {
        return List.of(new Attribute("wfSpecName", namePb.getWfSpecName()));
    }

    private List<Attribute> buildStatusAndSpecAttributesPb() {
        return Arrays.asList(
                new Attribute(
                        "wfSpecId",
                        LHSerializable.fromProto(statusAndSpec.getWfSpecId(), WfSpecIdModel.class)
                                .toString()),
                new Attribute("status", statusAndSpec.getStatus().toString()));
    }
}

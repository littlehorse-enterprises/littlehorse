package io.littlehorse.server.streams.lhinternalscan.publicrequests;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHStore;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.SearchWfRunRequest;
import io.littlehorse.sdk.common.proto.VariableMatch;
import io.littlehorse.sdk.common.proto.WfRunId;
import io.littlehorse.sdk.common.proto.WfRunIdList;
import io.littlehorse.server.streams.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streams.lhinternalscan.SearchScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.TagScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.SearchWfRunReply;
import io.littlehorse.server.streams.storeinternals.GetableIndex;
import io.littlehorse.server.streams.storeinternals.index.Attribute;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SearchWfRunRequestModel
extends PublicScanRequest<SearchWfRunRequest, WfRunIdList, WfRunId, WfRunIdModel, SearchWfRunReply> {

    // from proto
    private String wfSpecName;
    private Integer wfSpecMajorVersion;
    private Integer wfSpecRevision;
    private LHStatus status;
    private List<VariableMatchModel> variableMatches = new ArrayList<>();

    private Date earliestStart;
    private Date latestStart;

    // not from proto
    private ExecutionContext executionContext;

    public GetableClassEnum getObjectType() {
        return GetableClassEnum.WF_RUN;
    }

    public Class<SearchWfRunRequest> getProtoBaseClass() {
        return SearchWfRunRequest.class;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        SearchWfRunRequest p = (SearchWfRunRequest) proto;
        if (p.hasLimit()) limit = p.getLimit();
        if (p.hasBookmark()) {
            try {
                bookmark = BookmarkPb.parseFrom(p.getBookmark());
            } catch (Exception exn) {
                log.error("Failed to load bookmark: {}", exn.getMessage(), exn);
            }
        }

        wfSpecName = p.getWfSpecName();
        if (p.hasWfSpecMajorVersion()) wfSpecMajorVersion = p.getWfSpecMajorVersion();
        if (p.hasWfSpecRevision()) wfSpecRevision = p.getWfSpecRevision();
        if (p.hasStatus()) status = p.getStatus();

        if (p.hasEarliestStart()) earliestStart = LHUtil.fromProtoTs(p.getEarliestStart());
        if (p.hasLatestStart()) latestStart = LHUtil.fromProtoTs(p.getLatestStart());

        for (VariableMatch vm : p.getVariableFiltersList()) {
            variableMatches.add(LHSerializable.fromProto(vm, VariableMatchModel.class, context));
        }

        this.executionContext = context;
    }

    public SearchWfRunRequest.Builder toProto() {
        SearchWfRunRequest.Builder out = SearchWfRunRequest.newBuilder().setWfSpecName(wfSpecName);
        if (bookmark != null) {
            out.setBookmark(bookmark.toByteString());
        }
        if (limit != null) {
            out.setLimit(limit);
        }

        if (wfSpecMajorVersion != null) out.setWfSpecMajorVersion(wfSpecMajorVersion);
        if (wfSpecRevision != null) out.setWfSpecRevision(wfSpecRevision);

        if (earliestStart != null) out.setEarliestStart(LHUtil.fromDate(earliestStart));
        if (latestStart != null) out.setLatestStart(LHUtil.fromDate(latestStart));

        for (VariableMatchModel vmm : variableMatches) {
            out.addVariableFilters(vmm.toProto());
        }

        return out;
    }

    public static SearchWfRunRequestModel fromProto(SearchWfRunRequest proto, ExecutionContext context) {
        SearchWfRunRequestModel out = new SearchWfRunRequestModel();
        out.initFrom(proto, context);
        return out;
    }

    public List<Attribute> getSearchAttributes() {
        List<Attribute> out = new ArrayList<>();

        if (wfSpecMajorVersion != null) {
            if (wfSpecRevision == null) {
                out.add(new Attribute("majorVersion", wfSpecName + "/" + LHUtil.toLHDbVersionFormat(wfSpecMajorVersion)));
            } else {
                out.add(new Attribute(
                        "wfSpecId", new WfSpecIdModel(wfSpecName, wfSpecMajorVersion, wfSpecRevision).toString()));
            }
        } else {
            if (wfSpecRevision != null) {
                throw new LHApiException(
                        Status.INVALID_ARGUMENT, "Cannot provide wfSpecRevision without wfSpecMajorVersion");
            }
            out.add(new Attribute("wfSpecName", wfSpecName));
        }

        if (status != null) {
            out.add(new Attribute("status", status.toString()));
        }

        return out;
    }

    @Override
    public TagStorageType indexTypeForSearch() throws LHApiException {
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
        return indexTypeForSearch() == TagStorageType.LOCAL ? LHStore.CORE : LHStore.REPARTITION;
    }

    @Override
    public SearchScanBoundaryStrategy getScanBoundary(String searchAttributeString) {
        return new TagScanBoundaryStrategy(
                searchAttributeString, Optional.ofNullable(earliestStart), Optional.ofNullable(latestStart));
    }
}

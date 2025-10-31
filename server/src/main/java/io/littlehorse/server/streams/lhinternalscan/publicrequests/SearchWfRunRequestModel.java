package io.littlehorse.server.streams.lhinternalscan.publicrequests;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHStore;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadVarDefModel;
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
import io.littlehorse.server.streams.lhinternalscan.publicrequests.scanfilter.ScanFilterModel;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.SearchWfRunReply;
import io.littlehorse.server.streams.storeinternals.index.Attribute;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.RequestExecutionContext;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
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
    private WfRunIdModel parentWfRunId;

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
        if (p.hasParentWfRunId())
            parentWfRunId = LHSerializable.fromProto(p.getParentWfRunId(), WfRunIdModel.class, context);

        for (VariableMatch vm : p.getVariableFiltersList()) {
            variableMatches.add(LHSerializable.fromProto(vm, VariableMatchModel.class, context));
        }

        this.executionContext = context;
    }

    public SearchWfRunRequest.Builder toProto() {
        SearchWfRunRequest.Builder out = SearchWfRunRequest.newBuilder();
        if (wfSpecName != null) {
            out.setWfSpecName(wfSpecName);
        }
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
        if (parentWfRunId != null) out.setParentWfRunId(parentWfRunId.toProto());

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
        boolean hasName = wfSpecName != null && !wfSpecName.isEmpty();
        boolean hasMajor = wfSpecMajorVersion != null;
        boolean hasRevision = wfSpecRevision != null;

        if (!hasName && (hasMajor || hasRevision)) {
            throw new LHApiException(
                    Status.INVALID_ARGUMENT, "Cannot provide wfSpecMajorVersion or wfSpecRevision without wfSpecName");
        }

        if (hasRevision && !hasMajor) {
            throw new LHApiException(
                    Status.INVALID_ARGUMENT, "Cannot provide wfSpecRevision without wfSpecMajorVersion");
        }

        if (hasName) {
            if (hasMajor && hasRevision) {
                out.add(new Attribute(
                        "wfSpecId", new WfSpecIdModel(wfSpecName, wfSpecMajorVersion, wfSpecRevision).toString()));
            } else if (hasMajor) {
                out.add(new Attribute(
                        "majorVersion", wfSpecName + "/" + LHUtil.toLHDbVersionFormat(wfSpecMajorVersion)));
            } else {
                out.add(new Attribute("wfSpecName", wfSpecName));
            }
        }

        if (status != null) {
            out.add(new Attribute("status", status.toString()));
        }

        if (parentWfRunId != null) {
            out.add(new Attribute("parentWfRunId", parentWfRunId.toString()));
        }

        return out;
    }

    @Override
    public TagStorageType indexTypeForSearch() throws LHApiException {
        // This will be more complex when we have REMOTE tags again.
        return TagStorageType.LOCAL;
    }

    @Override
    public LHStore getStoreType() {
        return LHStore.CORE;
    }

    @Override
    public SearchScanBoundaryStrategy getScanBoundary(String searchAttributeString) {
        return new TagScanBoundaryStrategy(
                searchAttributeString, Optional.ofNullable(earliestStart), Optional.ofNullable(latestStart));
    }

    @Override
    public List<ScanFilterModel> getFilters(RequestExecutionContext ctx) throws LHApiException {
        // TODO: optimize it so that we send a Variable Search query before sending a WfRun Scan.

        List<ScanFilterModel> out = new ArrayList<>();
        if (wfSpecName == null || wfSpecName.isEmpty()) {
            if (!variableMatches.isEmpty()) {
                throw new LHApiException(
                        Status.INVALID_ARGUMENT, "Cannot filter by variables without specifying wfSpecName");
            }

            if (parentWfRunId != null && status != null) {
                out.add(new ScanFilterModel(status));
            }
            return out;
        }

        WfSpecModel wfSpec = ctx.service().getWfSpec(wfSpecName, wfSpecMajorVersion, wfSpecRevision);
        if (wfSpec == null) {
            throw new LHApiException(
                    Status.INVALID_ARGUMENT,
                    "Couldn't find wfSpec %s %d %d".formatted(wfSpecName, wfSpecMajorVersion, wfSpecRevision));
        }

        ThreadSpecModel thread = wfSpec.getThreadSpecs().get(wfSpec.getEntrypointThreadName());
        Map<String, ThreadVarDefModel> vars = thread.getVariableDefs().stream()
                .collect(Collectors.toMap(var -> var.getVarDef().getName(), var -> var));

        for (VariableMatchModel variableMatch : variableMatches) {
            ThreadVarDefModel var = vars.get(variableMatch.getVarName());

            if (var == null) {
                throw new LHApiException(
                        Status.INVALID_ARGUMENT,
                        "Variable %s not present in main thread of workflow %s"
                                .formatted(variableMatch.getVarName(), wfSpecName));
            }

            // TODO: Validation for the variable type.
            out.add(new ScanFilterModel(variableMatch));
        }

        return out;
    }
}

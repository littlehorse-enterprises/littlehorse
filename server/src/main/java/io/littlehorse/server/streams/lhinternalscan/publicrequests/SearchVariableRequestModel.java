package io.littlehorse.server.streams.lhinternalscan.publicrequests;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadVarDefModel;
import io.littlehorse.common.model.getable.objectId.VariableIdModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.LHStoreType;
import io.littlehorse.sdk.common.proto.SearchVariableRequest;
import io.littlehorse.sdk.common.proto.VariableId;
import io.littlehorse.sdk.common.proto.VariableIdList;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.server.streams.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.SearchVariableReply;
import io.littlehorse.server.streams.lhinternalscan.util.ScanBoundary;
import io.littlehorse.server.streams.lhinternalscan.util.TagScanModel;
import io.littlehorse.server.streams.storeinternals.index.Attribute;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.RequestExecutionContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SearchVariableRequestModel
        extends PublicScanRequest<
                SearchVariableRequest, VariableIdList, VariableId, VariableIdModel, SearchVariableReply> {

    // from proto
    private VariableValueModel value;
    private String varName;
    private String wfSpecName;
    private Integer wfSpecMajorVersion;
    private Integer wfSpecRevision;

    public GetableClassEnum getObjectType() {
        return GetableClassEnum.VARIABLE;
    }

    public Class<SearchVariableRequest> getProtoBaseClass() {
        return SearchVariableRequest.class;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext ctx) {
        SearchVariableRequest p = (SearchVariableRequest) proto;
        if (p.hasLimit()) limit = p.getLimit();
        if (p.hasBookmark()) {
            try {
                bookmark = BookmarkPb.parseFrom(p.getBookmark());
            } catch (Exception exn) {
                log.error("Failed to load bookmark: {}", exn.getMessage(), exn);
            }
        }

        varName = p.getVarName();
        wfSpecName = p.getWfSpecName();
        value = VariableValueModel.fromProto(p.getValue(), ctx);
        if (p.hasWfSpecMajorVersion()) wfSpecMajorVersion = p.getWfSpecMajorVersion();
        if (p.hasWfSpecRevision()) wfSpecRevision = p.getWfSpecRevision();
    }

    @Override
    public SearchVariableRequest.Builder toProto() {
        SearchVariableRequest.Builder out = SearchVariableRequest.newBuilder()
                .setVarName(varName)
                .setWfSpecName(wfSpecName)
                .setValue(value.toProto());

        if (bookmark != null) out.setBookmark(bookmark.toByteString());
        if (limit != null) out.setLimit(limit);

        if (wfSpecMajorVersion != null) out.setWfSpecMajorVersion(wfSpecMajorVersion);
        if (wfSpecRevision != null) out.setWfSpecRevision(wfSpecRevision);

        return out;
    }

    public static SearchVariableRequestModel fromProto(SearchVariableRequest proto, ExecutionContext context) {
        SearchVariableRequestModel out = new SearchVariableRequestModel();
        out.initFrom(proto, context);
        return out;
    }

    @Override
    public LHStoreType getStoreType() {
        // This will become more complex when we re-enable REMOTE tags. We will
        // likely need to pass in a DAO.
        return LHStoreType.CORE;
    }

    @Override
    public ScanBoundary<?, VariableIdModel> getScanBoundary(RequestExecutionContext ctx) throws LHApiException {
        // First, do some validation
        WfSpecModel spec = ctx.service().getWfSpec(wfSpecName, wfSpecMajorVersion, wfSpecRevision);
        if (spec == null) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "Couldn't find WfSpec %s".formatted(wfSpecName));
        }

        ThreadVarDefModel varDef = spec.getAllVariables().get(varName);
        if (varDef == null) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "Provided WfSpec has no variable named " + varName);
        }

        if (!varDef.isSearchable()) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "Provided variable has no index");
        }

        if (varDef.getVarDef().getType() != value.getType()) {
            throw new LHApiException(
                    Status.INVALID_ARGUMENT,
                    "Specified Variable has type " + varDef.getVarDef().getType());
        }

        // Now that we know it's a valid query, we can process it.
        TagScanModel<VariableIdModel> out = new TagScanModel<>(GetableClassEnum.VARIABLE);
        if (wfSpecMajorVersion != null) {
            if (wfSpecRevision == null) {
                throw new LHApiException(Status.INVALID_ARGUMENT, "If providing version, must also provide revision");
            }
            out.add(new Attribute(
                            "wfSpecId", new WfSpecIdModel(wfSpecName, wfSpecMajorVersion, wfSpecRevision).toString()))
                    .add(new Attribute(varName, getVariableValue(value.toProto().build())));
        } else {
            out.add(new Attribute("wfSpecName", wfSpecName))
                    .add(new Attribute("value", getVariableValue(value.toProto().build())));
        }

        return out;
    }

    private String getVariableValue(VariableValue value) throws LHApiException {
        return switch (value.getValueCase()) {
            case STR -> value.getStr();
            case BOOL -> String.valueOf(value.getBool());
            case INT -> String.valueOf(value.getInt());
            case DOUBLE -> String.valueOf(value.getDouble());
            default -> {
                throw new LHApiException(
                        Status.INVALID_ARGUMENT, "Search for %s not supported".formatted(value.getValueCase()));
            }
        };
    }
}

package io.littlehorse.server.streams.lhinternalscan.publicrequests;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHStore;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.core.variable.VariableModel;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.global.wfspec.TypeDefinitionModel;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadVarDefModel;
import io.littlehorse.common.model.getable.objectId.VariableIdModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.SearchVariableRequest;
import io.littlehorse.sdk.common.proto.VariableId;
import io.littlehorse.sdk.common.proto.VariableIdList;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.server.streams.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streams.lhinternalscan.SearchScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.TagScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.SearchVariableReply;
import io.littlehorse.server.streams.storeinternals.GetableIndex;
import io.littlehorse.server.streams.storeinternals.index.Attribute;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.WfService;
import java.util.List;
import java.util.Optional;
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

    // Not from proto
    private WfService service;

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

        // :porg:
        this.service = ctx.service();
    }

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

    private Optional<TagStorageType> getStorageTypeFromVariableIndexConfiguration() {
        return new VariableModel()
                .getIndexConfigurations().stream()
                        // Filter matching configuration
                        .filter(getableIndexConfiguration ->
                                getableIndexConfiguration.searchAttributesMatch(searchAttributesString()))
                        .map(GetableIndex::getTagStorageType)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .findFirst();
    }

    private TagStorageType indexTypeForSearchFromWfSpec() {
        WfSpecModel spec = service.getWfSpec(wfSpecName, wfSpecMajorVersion, wfSpecRevision);

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

        // ONLY do this check if the Variable is a PRIMITIVE type.
        // TODO: Extend this when implementing Struct and StructDef.
        TypeDefinitionModel varType = varDef.getVarDef().getTypeDef();
        if (isTypeSearchable(varType.getType()) && !varType.isCompatibleWith(value)) {
            throw new LHApiException(
                    Status.INVALID_ARGUMENT,
                    "Specified Variable has type " + varDef.getVarDef().getTypeDef());
        }

        // Currently, all tags are LOCAL
        return TagStorageType.LOCAL;
    }

    public List<Attribute> getSearchAttributes() throws LHApiException {
        if (wfSpecMajorVersion != null) {
            if (wfSpecRevision == null) {
                throw new LHApiException(Status.INVALID_ARGUMENT, "If providing version, must also provide revision");
            }
            return List.of(
                    new Attribute(
                            "wfSpecId", new WfSpecIdModel(wfSpecName, wfSpecMajorVersion, wfSpecRevision).toString()),
                    new Attribute(varName, getVariableValue(value.toProto().build())));
        } else {
            return List.of(
                    new Attribute("wfSpecName", wfSpecName),
                    new Attribute(varName, getVariableValue(value.toProto().build())));
        }
    }

    @Override
    public TagStorageType indexTypeForSearch() {
        return getStorageTypeFromVariableIndexConfiguration().orElseGet(() -> {
            TagStorageType result = indexTypeForSearchFromWfSpec();
            log.trace("Doing a {} search", result);
            return result;
        });
    }

    @Override
    public LHStore getStoreType() {
        // This will become more complex when we re-enable REMOTE tags. We will
        // likely need to pass in a DAO.
        return LHStore.CORE;
    }

    @Override
    public SearchScanBoundaryStrategy getScanBoundary(String searchAttributeString) {
        return new TagScanBoundaryStrategy(searchAttributeString, Optional.empty(), Optional.empty());
    }

    private String getVariableValue(VariableValue value) throws LHApiException {
        return switch (value.getValueCase()) {
            case STR -> LHUtil.toLHDbSearchFormat(value.getStr());
            case BOOL -> String.valueOf(value.getBool());
            case INT -> String.valueOf(value.getInt());
            case DOUBLE -> String.valueOf(value.getDouble());
            default -> {
                throw new LHApiException(
                        Status.INVALID_ARGUMENT, "Search for %s not supported".formatted(value.getValueCase()));
            }
        };
    }

    private List<String> searchAttributesString() {
        return List.of("name", "value", "wfSpecName", "wfSpecVersion");
    }

    private static boolean isTypeSearchable(VariableType type) {
        switch (type) {
            case INT:
            case BOOL:
            case DOUBLE:
            case STR:
                return true;
            case JSON_OBJ:
            case JSON_ARR:
            case BYTES:
            case UNRECOGNIZED:
            case WF_RUN_ID:
            default:
                break;
        }
        return false;
    }
}

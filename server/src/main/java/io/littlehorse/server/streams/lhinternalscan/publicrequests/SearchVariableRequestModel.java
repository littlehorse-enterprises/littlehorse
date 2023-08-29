package io.littlehorse.server.streams.lhinternalscan.publicrequests;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHStore;
import io.littlehorse.common.dao.ReadOnlyMetadataStore;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.core.variable.VariableModel;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableDefModel;
import io.littlehorse.common.model.getable.objectId.VariableIdModel;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.SearchVariableRequest;
import io.littlehorse.sdk.common.proto.SearchVariableRequest.NameAndValueRequest;
import io.littlehorse.sdk.common.proto.SearchVariableRequest.VariableCriteriaCase;
import io.littlehorse.sdk.common.proto.VariableId;
import io.littlehorse.sdk.common.proto.VariableIdList;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.server.streams.lhinternalscan.ObjectIdScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streams.lhinternalscan.SearchScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.TagScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.SearchVariableReply;
import io.littlehorse.server.streams.storeinternals.GetableIndex;
import io.littlehorse.server.streams.storeinternals.index.Attribute;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SearchVariableRequestModel
        extends PublicScanRequest<
                SearchVariableRequest, VariableIdList, VariableId, VariableIdModel, SearchVariableReply> {

    public VariableCriteriaCase type;
    public NameAndValueRequest value;
    public String wfRunId;

    public GetableClassEnum getObjectType() {
        return GetableClassEnum.VARIABLE;
    }

    public Class<SearchVariableRequest> getProtoBaseClass() {
        return SearchVariableRequest.class;
    }

    public void initFrom(Message proto) {
        SearchVariableRequest p = (SearchVariableRequest) proto;
        if (p.hasLimit()) limit = p.getLimit();
        if (p.hasBookmark()) {
            try {
                bookmark = BookmarkPb.parseFrom(p.getBookmark());
            } catch (Exception exn) {
                log.error("Failed to load bookmark: {}", exn.getMessage(), exn);
            }
        }

        type = p.getVariableCriteriaCase();
        switch (type) {
            case VALUE:
                value = p.getValue();
                break;
            case WF_RUN_ID:
                wfRunId = p.getWfRunId();
                break;
            case VARIABLECRITERIA_NOT_SET:
                throw new RuntimeException("Not possible");
        }
    }

    public SearchVariableRequest.Builder toProto() {
        SearchVariableRequest.Builder out = SearchVariableRequest.newBuilder();
        if (bookmark != null) {
            out.setBookmark(bookmark.toByteString());
        }
        if (limit != null) {
            out.setLimit(limit);
        }
        switch (type) {
            case VALUE:
                out.setValue(value);
            case WF_RUN_ID:
                out.setWfRunId(wfRunId);
                break;
            case VARIABLECRITERIA_NOT_SET:
                throw new RuntimeException("not possible");
        }

        return out;
    }

    public static SearchVariableRequestModel fromProto(SearchVariableRequest proto) {
        SearchVariableRequestModel out = new SearchVariableRequestModel();
        out.initFrom(proto);
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

    private TagStorageType indexTypeForSearchFromWfSpec(ReadOnlyMetadataStore stores) {
        WfSpecModel spec = stores.getWfSpec(value.getWfSpecName(), value.getWfSpecVersion());

        if (spec == null) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "Couldn't find WfSpec");
        }

        return spec.getThreadSpecs().entrySet().stream()
                .flatMap(stringThreadSpecEntry -> stringThreadSpecEntry.getValue().getVariableDefs().stream())
                .filter(variableDef -> variableDef.getName().equals(value.getVarName()))
                .filter(variableDef ->
                        variableDef.getType().equals(value.getValue().getType()))
                .map(VariableDefModel::getTagStorageType)
                .findFirst()
                .orElse(null);
    }

    public List<Attribute> getSearchAttributes() throws LHApiException {
        return List.of(
                new Attribute("wfSpecName", value.getWfSpecName()),
                new Attribute("wfSpecVersion", LHUtil.toLHDbVersionFormat(value.getWfSpecVersion())),
                new Attribute(value.getVarName(), getVariableValue(value.getValue())));
    }

    @Override
    public TagStorageType indexTypeForSearch(ReadOnlyMetadataStore stores) {
        return getStorageTypeFromVariableIndexConfiguration().orElseGet(() -> {
            TagStorageType result = indexTypeForSearchFromWfSpec(stores);
            log.trace("Doing a {} search", result);
            if (result == null) {
                throw new LHApiException(Status.INVALID_ARGUMENT, "No index configured for this variable search");
            }
            return result;
        });
    }

    @Override
    public LHStore getStore(ReadOnlyMetadataStore metaStore) {
        switch (type) {
            case WF_RUN_ID:
                return LHStore.CORE;
            case VALUE:
                return indexTypeForSearch(metaStore) == TagStorageType.LOCAL ? LHStore.CORE : LHStore.REPARTITION;
            case VARIABLECRITERIA_NOT_SET:
        }
        throw new LHApiException(Status.INVALID_ARGUMENT, "Didn't provide variable criteria");
    }

    @Override
    public SearchScanBoundaryStrategy getScanBoundary(String searchAttributeString) {
        if (type == VariableCriteriaCase.WF_RUN_ID) {
            return new ObjectIdScanBoundaryStrategy(wfRunId);
        } else if (type == VariableCriteriaCase.VALUE) {
            return new TagScanBoundaryStrategy(searchAttributeString, Optional.empty(), Optional.empty());
        }
        return null;
    }

    private String getVariableValue(VariableValue value) throws LHApiException {
        return switch (value.getType()) {
            case STR -> value.getStr();
            case BOOL -> String.valueOf(value.getBool());
            case INT -> String.valueOf(value.getInt());
            case DOUBLE -> String.valueOf(value.getDouble());
            default -> {
                throw new LHApiException(
                        Status.INVALID_ARGUMENT, "Search for %s not supported".formatted(value.getType()));
            }
        };
    }

    private List<String> searchAttributesString() {
        return List.of("name", "value", "wfSpecName", "wfSpecVersion");
    }
}

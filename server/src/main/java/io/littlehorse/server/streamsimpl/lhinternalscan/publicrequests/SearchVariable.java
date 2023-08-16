package io.littlehorse.server.streamsimpl.lhinternalscan.publicrequests;

import com.google.protobuf.Message;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.meta.VariableDefModel;
import io.littlehorse.common.model.meta.WfSpecModel;
import io.littlehorse.common.model.objectId.VariableId;
import io.littlehorse.common.model.wfrun.Variable;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GetableClassEnumPb;
import io.littlehorse.common.proto.TagStorageTypePb;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.SearchVariablePb;
import io.littlehorse.sdk.common.proto.SearchVariablePb.NameAndValuePb;
import io.littlehorse.sdk.common.proto.SearchVariablePb.VariableCriteriaCase;
import io.littlehorse.sdk.common.proto.SearchVariableReplyPb;
import io.littlehorse.sdk.common.proto.VariableIdPb;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.server.streamsimpl.lhinternalscan.ObjectIdScanBoundaryStrategy;
import io.littlehorse.server.streamsimpl.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streamsimpl.lhinternalscan.SearchScanBoundaryStrategy;
import io.littlehorse.server.streamsimpl.lhinternalscan.TagScanBoundaryStrategy;
import io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies.SearchVariableReply;
import io.littlehorse.server.streamsimpl.storeinternals.GetableIndex;
import io.littlehorse.server.streamsimpl.storeinternals.index.Attribute;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SearchVariable
    extends PublicScanRequest<SearchVariablePb, SearchVariableReplyPb, VariableIdPb, VariableId, SearchVariableReply> {

    public VariableCriteriaCase type;
    public NameAndValuePb value;
    public String wfRunId;
    private int wfSpecVersion;

    public GetableClassEnumPb getObjectType() {
        return GetableClassEnumPb.VARIABLE;
    }

    public Class<SearchVariablePb> getProtoBaseClass() {
        return SearchVariablePb.class;
    }

    public void initFrom(Message proto) {
        SearchVariablePb p = (SearchVariablePb) proto;
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

    public SearchVariablePb.Builder toProto() {
        SearchVariablePb.Builder out = SearchVariablePb.newBuilder();
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

    public static SearchVariable fromProto(SearchVariablePb proto) {
        SearchVariable out = new SearchVariable();
        out.initFrom(proto);
        return out;
    }

    private Optional<TagStorageTypePb> getStorageTypeFromVariableIndexConfiguration() {
        return new Variable()
            .getIndexConfigurations()
            .stream()
            //Filter matching configuration
            .filter(getableIndexConfiguration ->
                getableIndexConfiguration.searchAttributesMatch(
                    searchAttributesString()
                )
            )
            .map(GetableIndex::getTagStorageTypePb)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();
    }

    private TagStorageTypePb indexTypeForSearchFromWfSpec(LHGlobalMetaStores stores) {
        WfSpecModel spec = stores.getWfSpec(value.getWfSpecName(), null);

        return spec
            .getThreadSpecs()
            .entrySet()
            .stream()
            .flatMap(stringThreadSpecEntry ->
                stringThreadSpecEntry.getValue().getVariableDefs().stream()
            )
            .filter(variableDef -> variableDef.getName().equals(value.getVarName()))
            .filter(variableDef ->
                variableDef.getType().equals(value.getValue().getType())
            )
            .map(VariableDefModel::getTagStorageType)
            .findFirst()
            .orElse(null);
    }

    public List<Attribute> getSearchAttributes() throws LHValidationError {
        return List.of(
            new Attribute("wfSpecName", value.getWfSpecName()),
            new Attribute("wfSpecVersion", LHUtil.toLHDbVersionFormat(wfSpecVersion)),
            new Attribute(value.getVarName(), getVariableValue(value.getValue()))
        );
    }

    @Override
    public TagStorageTypePb indexTypeForSearch(LHGlobalMetaStores stores)
        throws LHValidationError {
        return getStorageTypeFromVariableIndexConfiguration()
            .orElse(indexTypeForSearchFromWfSpec(stores));
    }

    @Override
    public void validate() throws LHValidationError {}

    @Override
    public SearchScanBoundaryStrategy getScanBoundary(String searchAttributeString) {
        if (type == VariableCriteriaCase.WF_RUN_ID) {
            return new ObjectIdScanBoundaryStrategy(wfRunId);
        } else if (type == VariableCriteriaCase.VALUE) {
            return new TagScanBoundaryStrategy(
                searchAttributeString,
                Optional.empty(),
                Optional.empty()
            );
        }
        return null;
    }

    private String getVariableValue(VariableValue value) throws LHValidationError {
        return switch (value.getType()) {
            case STR -> value.getStr();
            case BOOL -> String.valueOf(value.getBool());
            case INT -> String.valueOf(value.getInt());
            case DOUBLE -> String.valueOf(value.getDouble());
            default -> {
                throw new LHValidationError(
                    "Search for %s not supported".formatted(value.getType())
                );
            }
        };
    }

    private List<String> searchAttributesString() {
        return List.of("name", "value", "wfSpecName", "wfSpecVersion");
    }
}

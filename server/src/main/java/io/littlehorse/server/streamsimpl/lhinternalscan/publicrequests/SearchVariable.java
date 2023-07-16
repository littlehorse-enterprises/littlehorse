package io.littlehorse.server.streamsimpl.lhinternalscan.publicrequests;

import com.google.protobuf.Message;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.Getable;
import io.littlehorse.common.model.meta.VariableDef;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.common.model.objectId.VariableId;
import io.littlehorse.common.model.wfrun.Variable;
import io.littlehorse.common.model.wfrun.WfRun;
import io.littlehorse.common.proto.AttributePb;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GetableClassEnumPb;
import io.littlehorse.common.proto.InternalScanPb.BoundedObjectIdScanPb;
import io.littlehorse.common.proto.InternalScanPb.ScanBoundaryCase;
import io.littlehorse.common.proto.InternalScanPb.TagScanPb;
import io.littlehorse.common.proto.ScanResultTypePb;
import io.littlehorse.common.proto.TagStorageTypePb;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.SearchVariablePb;
import io.littlehorse.sdk.common.proto.SearchVariablePb.NameAndValuePb;
import io.littlehorse.sdk.common.proto.SearchVariablePb.VariableCriteriaCase;
import io.littlehorse.sdk.common.proto.SearchVariableReplyPb;
import io.littlehorse.sdk.common.proto.VariableIdPb;
import io.littlehorse.sdk.common.proto.VariableValuePb;
import io.littlehorse.server.streamsimpl.ServerTopology;
import io.littlehorse.server.streamsimpl.lhinternalscan.InternalScan;
import io.littlehorse.server.streamsimpl.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies.SearchVariableReply;
import io.littlehorse.server.streamsimpl.storeinternals.GetableIndex;
import io.littlehorse.server.streamsimpl.storeinternals.index.Attribute;
import io.littlehorse.server.streamsimpl.storeinternals.index.Tag;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SearchVariable
    extends PublicScanRequest<SearchVariablePb, SearchVariableReplyPb, VariableIdPb, VariableId, SearchVariableReply> {

    public VariableCriteriaCase type;
    public NameAndValuePb value;
    public String wfRunId;

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

    public InternalScan startInternalSearch(LHGlobalMetaStores stores)
        throws LHValidationError {
        InternalScan out = new InternalScan();

        out.storeName = ServerTopology.CORE_STORE;
        out.resultType = ScanResultTypePb.OBJECT_ID;

        if (type == VariableCriteriaCase.WF_RUN_ID) {
            out.type = ScanBoundaryCase.BOUNDED_OBJECT_ID_SCAN;
            out.partitionKey = wfRunId;
            out.boundedObjectIdScan =
                BoundedObjectIdScanPb
                    .newBuilder()
                    .setStartObjectId(wfRunId + "/")
                    .setEndObjectId(wfRunId + "/~")
                    .build();
        } else if (type == VariableCriteriaCase.VALUE) {
            out.type = ScanBoundaryCase.TAG_SCAN;

            // This may change depending on the type of the tag. For example,
            // sparse strings (such as emails) may be REMOTE_HASH_UNCOUNTED; whereas
            // hot boolean variables may be LOCAL_UNCOUNTED
            out.partitionKey = null;

            int wfSpecVersion;
            if (value.hasWfSpecVersion()) {
                wfSpecVersion = value.getWfSpecVersion();
                WfSpec spec = stores.getWfSpec(value.getWfSpecName(), wfSpecVersion);
                if (spec == null) {
                    throw new LHValidationError(
                        null,
                        "Couldn't find specified wfSpec"
                    );
                }
            } else {
                WfSpec spec = stores.getWfSpec(value.getWfSpecName(), null);
                if (spec == null) {
                    throw new LHValidationError(
                        null,
                        "Search refers to missing WfSpec"
                    );
                } else {
                    wfSpecVersion = spec.version;
                }
            }
            List<Attribute> attributes = getAttributes(wfSpecVersion);
            List<AttributePb> attributesPb = attributes
                .stream()
                .map(Attribute::toProto)
                .map(AttributePb.Builder::build)
                .toList();
            out.tagScan =
                TagScanPb.newBuilder().addAllAttributes(attributesPb).build();
            Consumer<TagStorageTypePb> storageTypeSearchSetter = tagStorageTypePb -> {
                setSearchTypeFromTagStorageType(tagStorageTypePb, out, wfSpecVersion);
            };
            getStorageTypeFromVariableIndexConfiguration()
                .ifPresentOrElse(
                    storageTypeSearchSetter,
                    () -> setSearchTypeFromWfSpec(out, wfSpecVersion, stores)
                );
        }

        return out;
    }

    private Optional<TagStorageTypePb> getStorageTypeFromVariableIndexConfiguration() {
        return new Variable()
            .getIndexConfigurations()
            .stream()
            //Filter matching configuration
            .filter(getableIndexConfiguration ->
                getableIndexConfiguration.searchAttributesMatch(searchAttributes())
            )
            .map(GetableIndex::getTagStorageTypePb)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();
    }

    private void setSearchTypeFromWfSpec(
        InternalScan out,
        int wfSpecVersion,
        LHGlobalMetaStores stores
    ) {
        WfSpec spec = stores.getWfSpec(value.getWfSpecName(), null);
        spec
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
            .map(VariableDef::getTagStorageTypePb)
            .findFirst()
            .ifPresent(tagStorageTypePb -> {
                setSearchTypeFromTagStorageType(tagStorageTypePb, out, wfSpecVersion);
            });
    }

    private void setSearchTypeFromTagStorageType(
        TagStorageTypePb tagStorageTypePb,
        InternalScan out,
        int wfSpecVersion
    ) {
        if (tagStorageTypePb == TagStorageTypePb.LOCAL) {
            // Local Tag Scan (All Partitions Tag Scan)
            out.setStoreName(ServerTopology.CORE_STORE);
            out.setResultType(ScanResultTypePb.OBJECT_ID);
        } else {
            // Remote Tag Scan (Specific Partition Tag Scan)
            out.setStoreName(ServerTopology.CORE_REPARTITION_STORE);
            out.setResultType(ScanResultTypePb.OBJECT_ID);
            out.setPartitionKey(
                Tag.getAttributeString(
                    Getable.getTypeEnum(WfRun.class),
                    getAttributes(wfSpecVersion)
                )
            );
        }
    }

    private List<Attribute> getAttributes(int wfSpecVersion) {
        return List.of(
            new Attribute("name", value.getVarName()),
            new Attribute("value", getVariableValue(value.getValue())),
            new Attribute("wfSpecName", value.getWfSpecName()),
            new Attribute("wfSpecVersion", LHUtil.toLHDbVersionFormat(wfSpecVersion))
        );
    }

    private String getVariableValue(VariableValuePb value) {
        return value.getStr();
    }

    private List<String> searchAttributes() {
        return List.of("name", "value", "wfSpecName", "wfSpecVersion");
    }
}

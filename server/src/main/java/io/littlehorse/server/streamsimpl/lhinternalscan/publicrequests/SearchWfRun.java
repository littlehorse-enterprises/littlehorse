package io.littlehorse.server.streamsimpl.lhinternalscan.publicrequests;

import com.google.protobuf.Message;
import io.littlehorse.common.model.GETable;
import io.littlehorse.common.model.objectId.WfRunId;
import io.littlehorse.common.model.wfrun.WfRun;
import io.littlehorse.common.proto.AttributePb;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GETableClassEnumPb;
import io.littlehorse.common.proto.InternalScanPb.RemoteTagPrefixScanPb;
import io.littlehorse.common.proto.InternalScanPb.ScanBoundaryCase;
import io.littlehorse.common.proto.InternalScanPb.TagPrefixScanPb;
import io.littlehorse.common.proto.ScanResultTypePb;
import io.littlehorse.common.proto.TagStorageTypePb;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.jlib.common.proto.SearchWfRunPb;
import io.littlehorse.jlib.common.proto.SearchWfRunPb.NamePb;
import io.littlehorse.jlib.common.proto.SearchWfRunPb.StatusAndNamePb;
import io.littlehorse.jlib.common.proto.SearchWfRunPb.StatusAndSpecPb;
import io.littlehorse.jlib.common.proto.SearchWfRunPb.WfrunCriteriaCase;
import io.littlehorse.jlib.common.proto.SearchWfRunReplyPb;
import io.littlehorse.jlib.common.proto.WfRunIdPb;
import io.littlehorse.server.streamsimpl.ServerTopology;
import io.littlehorse.server.streamsimpl.lhinternalscan.InternalScan;
import io.littlehorse.server.streamsimpl.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies.SearchWfRunReply;
import io.littlehorse.server.streamsimpl.storeinternals.GETableIndex;
import io.littlehorse.server.streamsimpl.storeinternals.GETableIndexRegistry;
import io.littlehorse.server.streamsimpl.storeinternals.index.Attribute;
import io.littlehorse.server.streamsimpl.storeinternals.index.TagUtils;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.utils.CollectionUtils;

@Slf4j
public class SearchWfRun
    extends PublicScanRequest<SearchWfRunPb, SearchWfRunReplyPb, WfRunIdPb, WfRunId, SearchWfRunReply> {

    public WfrunCriteriaCase type;
    public StatusAndSpecPb statusAndSpec;
    private NamePb namePb;
    private StatusAndNamePb statusAndName;

    public GETableClassEnumPb getObjectType() {
        return GETableClassEnumPb.WF_RUN;
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

    public InternalScan startInternalSearch(LHGlobalMetaStores stores) {
        return startLocalInternalSearch();
    }

    private List<String> searchAttributes() {
        switch (type) {
            case STATUS_AND_SPEC:
                return Arrays.asList("wfSpecName", "status", "wfSpecVersion");
            case NAME:
                return Arrays.asList("wfSpecName");
            case STATUS_AND_NAME:
                return Arrays.asList("wfSpecName", "status");
            default:
                throw new RuntimeException("not possible");
        }
    }

    private InternalScan startLocalInternalSearch() {
        InternalScan out = new InternalScan();
        GETableIndex getableIndex = GETableIndexRegistry
            .getInstance()
            .findConfigurationForAttributes(WfRun.class, searchAttributes());
        List<Attribute> attributes = buildTagAttributes();
        // Converting to PB since this is a requirement for InternalScan#setLocalTagPrefixScan
        List<AttributePb> attributePbs = attributes
            .stream()
            .map(attribute -> attribute.toProto().build())
            .toList();
        if (getableIndex.getTagStorageTypePb() == TagStorageTypePb.LOCAL) {
            out.setStoreName(ServerTopology.CORE_STORE);
            out.setResultType(ScanResultTypePb.OBJECT_ID);
            out.setType(ScanBoundaryCase.LOCAL_TAG_PREFIX_SCAN);

            TagPrefixScanPb.Builder prefixScanBuilder = TagPrefixScanPb
                .newBuilder()
                .addAllAttributes(attributePbs);

            out.setLocalTagPrefixScan(prefixScanBuilder.build());
            if (statusAndSpec != null) {
                if (statusAndSpec.hasEarliestStart()) {
                    prefixScanBuilder.setEarliestCreateTime(
                        statusAndSpec.getEarliestStart()
                    );
                }
                if (statusAndSpec.hasLatestStart()) {
                    prefixScanBuilder.setLatestCreateTime(
                        statusAndSpec.getLatestStart()
                    );
                }
            }
            return out;
        } else {
            // REMOTE_UNCOUNTED
            out.setStoreName(ServerTopology.CORE_REPARTITION_STORE);
            out.setResultType(ScanResultTypePb.OBJECT_ID);
            out.setType(ScanBoundaryCase.REMOTE_TAG_PREFIX_SCAN);
            RemoteTagPrefixScanPb remoteTagBuilder = RemoteTagPrefixScanPb
                .newBuilder()
                .addAllAttributes(attributePbs)
                .build();
            out.setRemoteTagPrefixScanPb(remoteTagBuilder);
            out.setPartitionKey(getableIndex.getPartitionKeyForAttrs(attributes));
            return out;
        }
    }

    private List<Attribute> buildTagAttributes() {
        if (type == WfrunCriteriaCase.STATUS_AND_SPEC) {
            return buildStatusAndSpecAttributesPb();
        } else if (type == WfrunCriteriaCase.NAME) {
            return buildNameAttributePb();
        } else if (type == WfrunCriteriaCase.STATUS_AND_NAME) {
            return buildStatusAndNameAttributesPb();
        } else {
            throw new RuntimeException("Not possible or unimplemented");
        }
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

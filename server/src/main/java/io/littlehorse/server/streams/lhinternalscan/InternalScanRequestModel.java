package io.littlehorse.server.streams.lhinternalscan;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.kafka.common.utils.Utils;

import com.google.protobuf.Message;

import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.InternalScanRequest;
import io.littlehorse.common.proto.InternalScanRequest.ScanBoundaryCase;
import io.littlehorse.common.proto.InternalScanResponse;
import io.littlehorse.common.proto.ScanFilter;
import io.littlehorse.common.proto.ScanResultTypePb;
import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.lhinternalscan.publicrequests.scanfilter.ScanFilterModel;
import io.littlehorse.server.streams.lhinternalscan.util.BoundedObjectIdScanModel;
import io.littlehorse.server.streams.lhinternalscan.util.ScanBoundary;
import io.littlehorse.server.streams.lhinternalscan.util.TagScanModel;
import io.littlehorse.server.streams.store.LHIterKeyValue;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.RequestExecutionContext;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InternalScanRequestModel extends LHSerializable<InternalScanRequest> {

    private ScanResultTypePb resultType;
    private int limit;
    private BookmarkPb bookmark;
    private GetableClassEnum resultObjectType;
    private String storeName;

    private String partitionKey;

    private ScanBoundaryCase type;
    private TagScanModel tagScan;
    private BoundedObjectIdScanModel boundedObjectIdScan;

    private List<ScanFilterModel> filters = new ArrayList<>();

    // Below is not in the proto
    private final Pattern objectIdExtractorPattern = Pattern.compile("[0-9]+/[0-9]+/");

    public InternalScanRequestModel() {}

    public InternalScanRequestModel(ScanBoundary<?> boundary, RequestExecutionContext ctx) {
        this.setScanBoundary(boundary);
    }

    @Override
    public Class<InternalScanRequest> getProtoBaseClass() {
        return InternalScanRequest.class;
    }

    @Override
    public InternalScanRequest.Builder toProto() {
        InternalScanRequest.Builder out = InternalScanRequest.newBuilder()
                .setLimit(limit)
                .setStoreName(storeName)
                .setResultObjectType(resultObjectType)
                .setResultType(resultType);

        if (bookmark != null) out.setBookmark(bookmark);
        if (partitionKey != null) {
            out.setPartitionKey(partitionKey);
        }

        switch (type) {
            case TAG_SCAN:
                out.setTagScan(tagScan.toProto());
                break;
            case BOUNDED_OBJECT_ID_SCAN:
                out.setBoundedObjectIdScan(boundedObjectIdScan.toProto());
                break;
            case SCANBOUNDARY_NOT_SET:
                throw new RuntimeException("not possible");
        }

        for (ScanFilterModel filter : filters) {
            out.addFilters(filter.toProto());
        }

        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        InternalScanRequest p = (InternalScanRequest) proto;
        resultType = p.getResultType();
        limit = p.getLimit();
        if (p.hasBookmark()) bookmark = p.getBookmark();
        resultObjectType = p.getResultObjectType();
        storeName = p.getStoreName();

        if (p.hasPartitionKey()) partitionKey = p.getPartitionKey();

        type = p.getScanBoundaryCase();
        switch (type) {
            case TAG_SCAN:
                tagScan = LHSerializable.fromProto(p.getTagScan(), TagScanModel.class, context);
                break;
            case BOUNDED_OBJECT_ID_SCAN:
                boundedObjectIdScan =
                        LHSerializable.fromProto(p.getBoundedObjectIdScan(), BoundedObjectIdScanModel.class, context);
                break;
            case SCANBOUNDARY_NOT_SET:
                throw new RuntimeException("Not possible");
        }

        for (ScanFilter filter : p.getFiltersList()) {
            filters.add(LHSerializable.fromProto(filter, ScanFilterModel.class, context));
        }
    }

    public void scanPartition(RequestExecutionContext ctx, int partition, InternalScanResponse.Builder response) {
        if (query.getBookmark().getCompletedPartitionsList().contains(partition)) {
            throw new IllegalStateException("Scanning the same partition twice!");
        }
        ReadOnlyTenantScopedStore partitionStore = getStore(partition, query.getStoreName());

        PartitionBookmarkPb bkmk = query.getBookmark().getInProgressPartitionsOrDefault(partition, null);
        String startKey = bkmk == null ? query.getScanBoundary().getStartKey() : bkmk.getLastKey();
        String endKey = query.getScanBoundary().getEndKey();

        Class<? extends Storeable<?>> cls = query.getScanBoundary().getIterType();

        try (LHKeyValueIterator<?> iter = partitionStore.range(startKey, endKey, cls)) {
            while (iter.hasNext() && response.getResultsCount() < query.getLimit()) {
                LHIterKeyValue<?> next = iter.next();
                if (query.matches(next, executionContext())) {
                    response.addResults(query.convertToResult(next, executionContext()));
                }
            }

            if (iter.hasNext()) {
                String nextKey = iter.next().getKey();
                response.getUpdatedBookmarkBuilder()
                        .putInProgressPartitions(
                                partition,
                                PartitionBookmarkPb.newBuilder()
                                        .setParttion(partition)
                                        .setLastKey(nextKey)
                                        .build());
            } else {
                response.getUpdatedBookmarkBuilder().removeInProgressPartitions(partition);
                response.getUpdatedBookmarkBuilder().addCompletedPartitions(partition);
            }
        }
    }

    public ScanBoundary<?> getScanBoundary() {
        switch (type) {
            case TAG_SCAN:
                return tagScan;
            case BOUNDED_OBJECT_ID_SCAN:
                return boundedObjectIdScan;
            case SCANBOUNDARY_NOT_SET:
        }
        throw new IllegalStateException("Scan boundary wasn't set");
    }

    public void setScanBoundary(ScanBoundary<?> boundary) {
        if (boundary instanceof TagScanModel) {
            type = ScanBoundaryCase.TAG_SCAN;
            this.tagScan = (TagScanModel) boundary;
        } else if (boundary instanceof BoundedObjectIdScanModel) {
            type = ScanBoundaryCase.BOUNDED_OBJECT_ID_SCAN;
            boundedObjectIdScan = (BoundedObjectIdScanModel) boundary;
        } else {
            throw new IllegalArgumentException("Unrecognized ScanBoundary type %s"
                    .formatted(boundary.getClass().getSimpleName()));
        }
    }

    public boolean matches(LHIterKeyValue<?> record, RequestExecutionContext ctx) {
        // TODO: re-enable this
        return true;
    }

    /**
     * Returns the set of all partitions that might have data satisfying this search
     * and which have not already been satisfied by previous paginated requests, according
     * to the Bookmark.
     * @param config is needed to get the total number of partitions in the LH Cluster,
     * in case we need to translate partitionKey to a partition number.
     * @return incomplete partitions.
     */
    public Set<Integer> getIncompletePartitions(LHServerConfig config) {
        Set<Integer> out = new HashSet<>();

        if (storeName.equals(ServerTopology.METADATA_STORE)) {
            // Metadata store has only one partition
            out.add(0);
        } else if (partitionKey != null) {
            // Then we are looking at a specific partition on one of the core stores,
            // so we need to do some math to figure out which one.
            out.add(keyToPartition(partitionKey, config.getClusterPartitions()));
        } else {
            // Every partition in the cluster could have data, unless we've already
            // seen it (and it's marked completed in the bookmark)
            for (int i = 0; i < config.getClusterPartitions(); i++) {
                if (!bookmark.getCompletedPartitionsList().contains(i)) {
                    out.add(i);
                }
            }
        }
        return out;
    }

    private int keyToPartition(String key, int totalPartitions) {
        return Utils.toPositive(Utils.murmur2(key.getBytes())) % totalPartitions;
    }

    // private ByteString iterKeyValueToInternalScanResult(LHIterKeyValue<? extends Storeable<?>> next) {

    //     if (resultType == ScanResultTypePb.OBJECT) {
    //         StoredGetable<?, ?> storedGetable = (StoredGetable<?, ?>) next.getValue();

    //         return ByteString.copyFrom(storedGetable.getStoredObject().toBytes());

    //     } else if (resultType == ScanResultTypePb.OBJECT_ID) {
    //         Class<? extends ObjectIdModel<?, ?, ?>> idCls = AbstractGetable.getIdCls(scanObjectType);

    //         // TODO: This is a leaky abstraction.
    //         String storeableKey = next.getKey();
    //         Matcher matcher = objectIdExtractorPattern.matcher(storeableKey);
    //         if (matcher.find()) {
    //             int prefixEndIndex = matcher.end(0);
    //             String objectIdStr = storeableKey.substring(prefixEndIndex);
    //             return ByteString.copyFrom(
    //                     ObjectIdModel.fromString(objectIdStr, idCls).toBytes());
    //         } else {
    //             throw new IllegalStateException("Invalid object id");
    //         }
    //     } else {
    //         throw new RuntimeException("Impossible: unknown result type");
    //     }
    // }
}

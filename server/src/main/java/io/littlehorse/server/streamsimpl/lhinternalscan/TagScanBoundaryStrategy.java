package io.littlehorse.server.streamsimpl.lhinternalscan;

import com.google.protobuf.Message;
import io.littlehorse.common.proto.InternalScanPb;
import io.littlehorse.common.util.LHUtil;
import java.util.Date;
import java.util.Optional;
import java.util.function.Consumer;

public class TagScanBoundaryStrategy implements SearchScanBoundaryStrategy {

    private String keyPrefix;
    private Optional<Date> earliestStart;
    private Optional<Date> latestStart;

    public TagScanBoundaryStrategy(
        String keyPrefix,
        Optional<Date> earliestStart,
        Optional<Date> latestStart
    ) {
        this.keyPrefix = keyPrefix;
        this.earliestStart = earliestStart;
        this.latestStart = latestStart;
    }

    @Override
    public Message buildScanProto() {
        InternalScanPb.TagScanPb.Builder prefixScanBuilder = InternalScanPb.TagScanPb.newBuilder();
        prefixScanBuilder.setKeyPrefix(keyPrefix);
        Consumer<Date> setLatestStartToBuilder = date -> {
            prefixScanBuilder.setLatestCreateTime(LHUtil.fromDate(date));
        };
        Consumer<Date> setEarliestStartToBuilder = date -> {
            prefixScanBuilder.setLatestCreateTime(LHUtil.fromDate(date));
        };
        earliestStart.ifPresent(setEarliestStartToBuilder);
        latestStart.ifPresent(setLatestStartToBuilder);
        return prefixScanBuilder.build();
    }

    @Override
    public String getSearchAttributeString() {
        return keyPrefix;
    }
}

package io.littlehorse.server.streams.util;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import io.littlehorse.common.model.GlobalGetable;
import io.littlehorse.common.model.getable.MetadataId;
import io.littlehorse.common.model.getable.ObjectIdModel;
import io.littlehorse.common.model.getable.objectId.TaskDefIdModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.StoreableType;
import io.littlehorse.common.proto.StoredGetablePb;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.WfSpec;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.utils.Bytes;

@Slf4j
public class MetadataCache extends LHCache<Message, Message> {

    private static final Pattern CACHEABLE_KEY_PATTERN =
            Pattern.compile(StoreableType.STORED_GETABLE_VALUE + "\\/(?<getableType>\\d+)\\/(?<key>.+)");
    public static final int LATEST_VERSION = -1;

    public MetadataCache() {}

    public void updateCache(String storeKey, Bytes value) throws LHSerdeError {
        Matcher keyMatcher = CACHEABLE_KEY_PATTERN.matcher(storeKey);
        boolean isCacheableKey = keyMatcher.matches();

        if (isCacheableKey) {
            String key = keyMatcher.group("key");
            int getableTypeNumber = Integer.parseInt(keyMatcher.group("getableType"));
            GetableClassEnum getableType = GetableClassEnum.forNumber(getableTypeNumber);
            if (getableType != null) {
                updateCache(value, getableType, key);
            } else {
                log.warn("No GetableClassEnum found for number: {}", getableTypeNumber);
            }
        }
    }

    private void updateCache(Bytes value, GetableClassEnum getableType, String key) throws LHSerdeError {
        switch (getableType) {
            case WF_SPEC -> {
                WfSpecIdModel id = (WfSpecIdModel) ObjectIdModel.fromString(key, WfSpecIdModel.class);
                WfSpecIdModel latestVersionId = new WfSpecIdModel(id.getName(), LATEST_VERSION);
                evictOrUpdate(value, id);
                evictOrUpdate(value, latestVersionId);
            }
            case TASK_DEF -> {
                TaskDefIdModel id = (TaskDefIdModel) ObjectIdModel.fromString(key, TaskDefIdModel.class);
                evictOrUpdate(value, id);
            }
            default -> {
                // nothing to do
            }
        }
    }

    private <U extends Message, V extends GlobalGetable<U>> void evictOrUpdate(
            Bytes value, MetadataId<?, U, V> cacheKey) throws LHSerdeError {
        String keyType = cacheKey.getObjectClass().getSimpleName();
        Message key = cacheKey.toProto().build();
        if (value == null) {
            log.trace("Evicting cache for {} with key {}", cacheKey, cacheKey);
            evictCache(key);
        } else {
            log.trace("Updating cache for {} with key {}", keyType, cacheKey);

            try {
                updateCache(key, StoredGetablePb.parseFrom(value.get()));
            } catch (InvalidProtocolBufferException e) {
                log.error("Cache cannot be updated at this time.");
            }
        }
    }

    public WfSpec getOrCache(String name, Integer version, Supplier<WfSpec> cacheable) {
        if (version == null) {
            version = LATEST_VERSION;
        }
        WfSpecIdModel cachedId = new WfSpecIdModel(name, version);
        return (WfSpec) getOrCache(cachedId.toProto().build(), cacheable::get);
    }
}

package io.littlehorse.server.streams.util;

import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.MetadataId;
import io.littlehorse.common.model.getable.global.taskdef.TaskDefModel;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.objectId.TaskDefIdModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.TaskDef;
import io.littlehorse.sdk.common.proto.WfSpec;
import io.littlehorse.server.streams.store.StoredGetable;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.utils.Bytes;

@Slf4j
public class MetadataCache extends LHCache<MetadataId<?, ?, ?>, Object> {

    private static final Pattern WFSPEC_KEY_PATTERN = Pattern.compile("0\\/2\\/(?<name>.+)\\/(?<version>\\d+)");
    private static final Pattern TASKDEF_KEY_PATTERN = Pattern.compile("0\\/0\\/(?<name>.+)");
    public static final int LATEST_VERSION = -1;

    public MetadataCache() {}

    public void addToCache(String key, Bytes value) throws LHSerdeError {
        Matcher wfSpecMatcher = WFSPEC_KEY_PATTERN.matcher(key);
        Matcher taskDefMatcher = TASKDEF_KEY_PATTERN.matcher(key);
        boolean isWfSpec = wfSpecMatcher.matches();
        boolean isTaskDef = taskDefMatcher.matches();

        if (isWfSpec) {
            String name = wfSpecMatcher.group("name");
            int version = Integer.parseInt(wfSpecMatcher.group("version"));
            WfSpecIdModel cacheVersionKey = new WfSpecIdModel(name, version);
            WfSpecIdModel cacheLatestKey = new WfSpecIdModel(name, LATEST_VERSION);
            if (value == null) {
                log.trace("Evicting wfSpecCache for {}", cacheVersionKey);
                evictCache(cacheVersionKey);
                evictCache(cacheLatestKey);
            } else {
                StoredGetable<WfSpec, WfSpecModel> wfSpecModel =
                        LHSerializable.fromBytes(value.get(), StoredGetable.class);
                log.trace("Updating wfSpecCache for {}", cacheVersionKey);
                updateCache(cacheVersionKey, wfSpecModel.getStoredObject());
                updateCache(cacheLatestKey, wfSpecModel.getStoredObject());
            }
        } else if (isTaskDef) {
            String name = taskDefMatcher.group("name");
            TaskDefIdModel cacheKey = new TaskDefIdModel(name);
            if (value == null) {
                log.trace("Evicting taskDefCache for {}", cacheKey);
                evictCache(cacheKey);
            } else {
                StoredGetable<TaskDef, TaskDefModel> taskDef =
                        LHSerializable.fromBytes(value.get(), StoredGetable.class);
                log.trace("Updating taskDefCache for {}", cacheKey);
                updateCache(cacheKey, taskDef.getStoredObject());
            }
        }
    }

    public WfSpecModel getOrCache(String name, Integer version, Supplier<WfSpecModel> cacheable) {
        if (version == null) {
            version = LATEST_VERSION;
        }
        WfSpecIdModel cachedId = new WfSpecIdModel(name, version);
        return (WfSpecModel) getOrCache(cachedId, cacheable::get);
    }
}

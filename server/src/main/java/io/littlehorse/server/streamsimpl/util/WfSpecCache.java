package io.littlehorse.server.streamsimpl.util;

import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.common.model.objectId.WfSpecId;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.utils.Bytes;

@Slf4j
public class WfSpecCache extends LHCache<WfSpecId, WfSpec> {

    private static final Pattern WFSPEC_KEY_PATTERN = Pattern.compile(
        "WfSpec\\/(?<name>.+)\\/(?<version>\\d+)"
    );
    public static final int LATEST_VERSION = -1;

    public WfSpecCache() {}

    public void addToCache(String key, Bytes value) throws LHSerdeError {
        Matcher wfSpecMatcher = WFSPEC_KEY_PATTERN.matcher(key);
        boolean isWfSpec = wfSpecMatcher.matches();

        if (isWfSpec) {
            String name = wfSpecMatcher.group("name");
            Integer version = Integer.valueOf(wfSpecMatcher.group("version"));
            WfSpecId cacheVersionKey = new WfSpecId(name, version);
            WfSpecId cacheLatestKey = new WfSpecId(name, LATEST_VERSION);
            if (value == null) {
                log.trace("Evicting wfSpecCache for {}", cacheVersionKey);
                evictCache(cacheVersionKey);
                evictCache(cacheLatestKey);
            } else {
                WfSpec wfSpec = WfSpec.fromBytes(value.get(), WfSpec.class, null);
                log.trace("Updating wfSpecCache for {}", cacheVersionKey);
                updateCache(cacheVersionKey, wfSpec);
                updateCache(cacheLatestKey, wfSpec);
            }
        }
    }

    public WfSpec getOrCache(
        String name,
        Integer version,
        Supplier<WfSpec> cacheable
    ) {
        if (version == null) {
            version = LATEST_VERSION;
        }
        WfSpecId cachedId = new WfSpecId(name, version);
        return getOrCache(cachedId, cacheable);
    }
}

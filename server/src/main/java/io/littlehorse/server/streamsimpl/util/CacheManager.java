package io.littlehorse.server.streamsimpl.util;

import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.common.utils.Bytes;

@Slf4j
public class CacheManager {

    private static final Pattern WFSPEC_KEY_PATTERN = Pattern.compile(
        "WfSpec\\/(?<name>.+)\\/(?<version>\\d+)"
    );
    private final LHCache<Pair<String, Integer>, WfSpec> wfSpecCache;

    public CacheManager(LHCache<Pair<String, Integer>, WfSpec> wfSpecCache) {
        this.wfSpecCache = wfSpecCache;
    }

    public void addToCache(String key, Bytes value) throws LHSerdeError {
        Matcher wfSpecMatcher = WFSPEC_KEY_PATTERN.matcher(key);
        boolean isWfSpec = wfSpecMatcher.matches();

        if (isWfSpec) {
            String name = wfSpecMatcher.group("name");
            Integer version = Integer.valueOf(wfSpecMatcher.group("version"));
            Pair<String, Integer> cacheVersionKey = Pair.of(name, version);
            Pair<String, Integer> cacheLatestKey = Pair.of(name, null);
            if (value == null) {
                wfSpecCache.evictCache(cacheVersionKey);
                wfSpecCache.evictCache(cacheLatestKey);
            } else {
                WfSpec wfSpec = WfSpec.fromBytes(value.get(), WfSpec.class, null);
                wfSpecCache.updateCache(cacheVersionKey, wfSpec);
                wfSpecCache.updateCache(cacheLatestKey, wfSpec);
            }
        }
    }
}

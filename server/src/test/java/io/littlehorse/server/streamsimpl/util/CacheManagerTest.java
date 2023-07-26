package io.littlehorse.server.streamsimpl.util;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import io.littlehorse.TestUtil;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.server.streamsimpl.storeinternals.utils.StoreUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.common.utils.Bytes;
import org.junit.jupiter.api.Test;

class CacheManagerTest {

    @Test
    public void shouldAddDeserializedWfSpecWithVersionToCache() throws LHSerdeError {
        final LHCache<Pair<String, Integer>, WfSpec> cache = new LHCache<>();
        final CacheManager cacheManager = new CacheManager(cache);
        final String wfSpecName = "WF1";
        final int wfSpecVersion = 23;
        final WfSpec wfSpec = TestUtil.wfSpec(wfSpecName);
        wfSpec.setVersion(wfSpecVersion);
        final String key = StoreUtils.getFullStoreKey(
            wfSpec.getObjectId().getStoreKey(),
            WfSpec.class
        );
        final Bytes value = Bytes.wrap(wfSpec.toBytes(null));

        cacheManager.addToCache(key, value);

        WfSpec cachedWfSpec = cache.get(Pair.of(wfSpecName, wfSpecVersion));

        assertThat(cachedWfSpec)
            .usingRecursiveComparison()
            .ignoringFields("threadSpecs")
            .isEqualTo(wfSpec);
    }

    @Test
    public void shouldAddDeserializedWfSpecWithLatestToCache() throws LHSerdeError {
        final LHCache<Pair<String, Integer>, WfSpec> cache = new LHCache<>();
        final CacheManager cacheManager = new CacheManager(cache);
        final String wfSpecName = "WF1";
        final int wfSpecVersion = 23;
        final WfSpec wfSpec = TestUtil.wfSpec(wfSpecName);
        wfSpec.setVersion(wfSpecVersion);
        final String key = StoreUtils.getFullStoreKey(
            wfSpec.getObjectId().getStoreKey(),
            WfSpec.class
        );
        final Bytes value = Bytes.wrap(wfSpec.toBytes(null));

        cacheManager.addToCache(key, value);

        WfSpec cachedLatestWfSpec = cache.get(Pair.of(wfSpecName, null));

        assertThat(cachedLatestWfSpec)
            .usingRecursiveComparison()
            .ignoringFields("threadSpecs")
            .isEqualTo(wfSpec);
    }

    @Test
    public void shouldEvictWfSpecWithVersionFromCacheWhenValueIsNull()
        throws LHSerdeError {
        final LHCache<Pair<String, Integer>, WfSpec> cache = new LHCache<Pair<String, Integer>, WfSpec>();
        final CacheManager cacheManager = new CacheManager(cache);
        final String key = "WfSpec/WF1/23";
        final Pair<String, Integer> cacheKey = Pair.of("WF1", 23);
        final Bytes value = null;

        cache.updateCache(cacheKey, TestUtil.wfSpec("WF1"));

        cacheManager.addToCache(key, value);

        assertThat(cache.get(cacheKey)).isNull();
    }

    @Test
    public void shouldEvictLatestWfSpecFromCacheWhenValueIsNull()
        throws LHSerdeError {
        final LHCache<Pair<String, Integer>, WfSpec> cache = new LHCache<Pair<String, Integer>, WfSpec>();
        final CacheManager cacheManager = new CacheManager(cache);
        final String key = "WfSpec/WF1/23";
        final Pair<String, Integer> latestCacheKey = Pair.of("WF1", null);
        final Bytes value = null;

        cache.updateCache(latestCacheKey, TestUtil.wfSpec("WF1"));

        cacheManager.addToCache(key, value);

        assertThat(cache.get(latestCacheKey)).isNull();
    }

    @Test
    public void shouldNotCacheKeysThatAreNotWfSpec() throws LHSerdeError {
        final LHCache<Pair<String, Integer>, WfSpec> cache = new LHCache<Pair<String, Integer>, WfSpec>();
        final CacheManager cacheManager = new CacheManager(cache);
        final String nonWfSpecKey =
            "TaskRun/WF1/123/0b80d81e-8984-4da5-8312-f19e3fbfa780";
        final Pair<String, Integer> cacheKey = Pair.of("WF1", 23);
        final Bytes value = Bytes.wrap(TestUtil.taskRun().toBytes(null));

        cacheManager.addToCache(nonWfSpecKey, value);

        assertThat(cache.get(cacheKey)).isNull();
    }
}

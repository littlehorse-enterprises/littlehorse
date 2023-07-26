package io.littlehorse.server.streamsimpl.util;

import static io.littlehorse.server.streamsimpl.util.WfSpecCache.LATEST_VERSION;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import io.littlehorse.TestUtil;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.common.model.objectId.WfSpecId;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.server.streamsimpl.storeinternals.utils.StoreUtils;
import org.apache.kafka.common.utils.Bytes;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class WfSpecCacheTest {

    @Nested
    class AddToCache {

        @Test
        public void shouldAddDeserializedWfSpecWithVersionToCache()
            throws LHSerdeError {
            final WfSpecCache wfSpecCache = new WfSpecCache();
            final String wfSpecName = "WF1";
            final int wfSpecVersion = 23;
            final WfSpec wfSpec = TestUtil.wfSpec(wfSpecName);
            wfSpec.setVersion(wfSpecVersion);
            final String key = StoreUtils.getFullStoreKey(
                wfSpec.getObjectId().getStoreKey(),
                WfSpec.class
            );
            final Bytes value = Bytes.wrap(wfSpec.toBytes(null));

            wfSpecCache.addToCache(key, value);

            WfSpec cachedWfSpec = wfSpecCache.get(
                new WfSpecId(wfSpecName, wfSpecVersion)
            );

            assertThat(cachedWfSpec)
                .usingRecursiveComparison()
                .ignoringFields("threadSpecs")
                .isEqualTo(wfSpec);
        }

        @Test
        public void shouldAddDeserializedWfSpecWithLatestToCache()
            throws LHSerdeError {
            final WfSpecCache wfSpecCache = new WfSpecCache();
            final String wfSpecName = "WF1";
            final int wfSpecVersion = 23;
            final WfSpec wfSpec = TestUtil.wfSpec(wfSpecName);
            wfSpec.setVersion(wfSpecVersion);
            final String key = StoreUtils.getFullStoreKey(
                wfSpec.getObjectId().getStoreKey(),
                WfSpec.class
            );
            final Bytes value = Bytes.wrap(wfSpec.toBytes(null));

            wfSpecCache.addToCache(key, value);

            WfSpec cachedLatestWfSpec = wfSpecCache.get(
                new WfSpecId(wfSpecName, LATEST_VERSION)
            );

            assertThat(cachedLatestWfSpec)
                .usingRecursiveComparison()
                .ignoringFields("threadSpecs")
                .isEqualTo(wfSpec);
        }

        @Test
        public void shouldEvictWfSpecWithVersionFromCacheWhenValueIsNull()
            throws LHSerdeError {
            final WfSpecCache wfSpecCache = new WfSpecCache();
            final String key = "WfSpec/WF1/23";
            final WfSpecId cacheKey = new WfSpecId("WF1", 23);
            final Bytes value = null;

            wfSpecCache.updateCache(cacheKey, TestUtil.wfSpec("WF1"));

            wfSpecCache.addToCache(key, value);

            assertThat(wfSpecCache.get(cacheKey)).isNull();
        }

        @Test
        public void shouldEvictLatestWfSpecFromCacheWhenValueIsNull()
            throws LHSerdeError {
            final WfSpecCache wfSpecCache = new WfSpecCache();
            final String key = "WfSpec/WF1/23";
            final WfSpecId latestCacheKey = new WfSpecId("WF1", LATEST_VERSION);
            final Bytes value = null;

            wfSpecCache.updateCache(latestCacheKey, TestUtil.wfSpec("WF1"));

            wfSpecCache.addToCache(key, value);

            assertThat(wfSpecCache.get(latestCacheKey)).isNull();
        }

        @Test
        public void shouldNotCacheKeysThatAreNotWfSpec() throws LHSerdeError {
            final WfSpecCache wfSpecCache = new WfSpecCache();
            final String nonWfSpecKey =
                "TaskRun/WF1/123/0b80d81e-8984-4da5-8312-f19e3fbfa780";
            final WfSpecId cacheKey = new WfSpecId("WF1", 23);
            final Bytes value = Bytes.wrap(TestUtil.taskRun().toBytes(null));

            wfSpecCache.addToCache(nonWfSpecKey, value);

            assertThat(wfSpecCache.get(cacheKey)).isNull();
        }
    }

    @Nested
    class GetOrCache {

        @Test
        public void shouldNotCacheNullValues() {
            final WfSpecCache wfSpecCache = new WfSpecCache();
            final WfSpecId cacheKey = new WfSpecId("WF1", 23);

            wfSpecCache.getOrCache(cacheKey, () -> null);

            assertThat(wfSpecCache.get(cacheKey)).isNull();
        }
    }
}

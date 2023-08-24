package io.littlehorse.server.streamsimpl.util;

import static io.littlehorse.server.streams.util.MetadataCache.LATEST_VERSION;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import io.littlehorse.TestUtil;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.server.streams.store.StoredGetable;
import io.littlehorse.server.streams.util.MetadataCache;
import org.apache.kafka.common.utils.Bytes;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class WfSpecModelCacheTest {

    @Nested
    class AddToCache {

        @Test
        public void shouldAddDeserializedWfSpecWithVersionToCache() throws LHSerdeError {
            final MetadataCache wfSpecCache = new MetadataCache();
            final String wfSpecName = "WF1";
            final int wfSpecVersion = 23;
            final WfSpecModel wfSpecModel = TestUtil.wfSpec(wfSpecName);
            wfSpecModel.setVersion(wfSpecVersion);
            final String key = StoredGetable.getStoreKey(wfSpecModel.getObjectId());
            final Bytes value = Bytes.wrap(wfSpecModel.toBytes());

            wfSpecCache.addToCache(key, value);

            WfSpecModel cachedWfSpecModel = wfSpecCache.get(new WfSpecIdModel(wfSpecName, wfSpecVersion));

            assertThat(cachedWfSpecModel)
                    .usingRecursiveComparison()
                    .ignoringFields("threadSpecs")
                    .isEqualTo(wfSpecModel);
        }

        @Test
        public void shouldAddDeserializedWfSpecWithLatestToCache() throws LHSerdeError {
            final MetadataCache wfSpecCache = new MetadataCache();
            final String wfSpecName = "WF1";
            final int wfSpecVersion = 23;
            final WfSpecModel wfSpecModel = TestUtil.wfSpec(wfSpecName);
            wfSpecModel.setVersion(wfSpecVersion);
            final String key = StoredGetable.getStoreKey(wfSpecModel.getObjectId());
            final Bytes value = Bytes.wrap(wfSpecModel.toBytes());

            wfSpecCache.addToCache(key, value);

            WfSpecModel cachedLatestWfSpecModel = wfSpecCache.get(new WfSpecIdModel(wfSpecName, LATEST_VERSION));

            assertThat(cachedLatestWfSpecModel)
                    .usingRecursiveComparison()
                    .ignoringFields("threadSpecs")
                    .isEqualTo(wfSpecModel);
        }

        @Test
        public void shouldEvictWfSpecWithVersionFromCacheWhenValueIsNull() throws LHSerdeError {
            final MetadataCache wfSpecCache = new MetadataCache();
            final String key = "2/WF1/23";
            final WfSpecIdModel cacheKey = new WfSpecIdModel("WF1", 23);
            final Bytes value = null;

            wfSpecCache.updateCache(cacheKey, TestUtil.wfSpec("WF1"));

            wfSpecCache.addToCache(key, value);

            assertThat(wfSpecCache.get(cacheKey)).isNull();
        }

        @Test
        public void shouldEvictLatestWfSpecFromCacheWhenValueIsNull() throws LHSerdeError {
            final MetadataCache wfSpecCache = new MetadataCache();
            final String key = "2/WF1/23";
            final WfSpecIdModel latestCacheKey = new WfSpecIdModel("WF1", LATEST_VERSION);
            final Bytes value = null;

            wfSpecCache.updateCache(latestCacheKey, TestUtil.wfSpec("WF1"));

            wfSpecCache.addToCache(key, value);

            assertThat(wfSpecCache.get(latestCacheKey)).isNull();
        }

        @Test
        public void shouldNotCacheKeysThatAreNotWfSpec() throws LHSerdeError {
            final MetadataCache wfSpecCache = new MetadataCache();
            final String nonWfSpecKey = "11/WF1/123/0b80d81e-8984-4da5-8312-f19e3fbfa780";
            final WfSpecIdModel cacheKey = new WfSpecIdModel("WF1", 23);
            final Bytes value = Bytes.wrap(TestUtil.taskRun().toBytes());

            wfSpecCache.addToCache(nonWfSpecKey, value);

            assertThat(wfSpecCache.get(cacheKey)).isNull();
        }
    }

    @Nested
    class GetOrCache {

        @Test
        public void shouldNotCacheNullValues() {
            final MetadataCache wfSpecCache = new MetadataCache();
            String name = "WF1";
            int version = 23;
            final WfSpecIdModel cacheKey = new WfSpecIdModel(name, version);

            wfSpecCache.getOrCache(cacheKey, () -> null);
            wfSpecCache.getOrCache(name, version, () -> null);

            assertThat(wfSpecCache.get(cacheKey)).isNull();
        }

        @Test
        public void shouldCreateLatestVersionWhenVersionIsNull() {
            final MetadataCache wfSpecCache = new MetadataCache();
            String name = "WF1";
            Integer version = null;
            WfSpecModel wfSpecModel = TestUtil.wfSpec(name);

            wfSpecCache.getOrCache(name, version, () -> wfSpecModel);

            WfSpecModel cachedLatestWfSpecModel = wfSpecCache.get(new WfSpecIdModel(name, LATEST_VERSION));

            assertThat(cachedLatestWfSpecModel)
                    .usingRecursiveComparison()
                    .ignoringFields("threadSpecs")
                    .isEqualTo(wfSpecModel);
        }
    }
}

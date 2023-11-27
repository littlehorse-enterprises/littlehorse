package io.littlehorse.server.streamsimpl.util;

import static io.littlehorse.server.streams.util.MetadataCache.LATEST_VERSION;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.google.protobuf.InvalidProtocolBufferException;
import io.littlehorse.TestUtil;
import io.littlehorse.common.model.getable.global.taskdef.TaskDefModel;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.objectId.TaskDefIdModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.proto.StoredGetablePb;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.TaskDef;
import io.littlehorse.sdk.common.proto.WfSpec;
import io.littlehorse.server.streams.store.StoredGetable;
import io.littlehorse.server.streams.util.MetadataCache;
import org.apache.kafka.common.utils.Bytes;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class WfSpecModelCacheTest {

    @Nested
    class UpdateCache {

        @Test
        public void shouldAddDeserializedWfSpecWithVersionToCache() throws InvalidProtocolBufferException {
            final MetadataCache metadataCache = new MetadataCache();
            final String wfSpecName = "WF1";
            final int wfSpecVersion = 23;
            final WfSpecModel wfSpecModel = TestUtil.wfSpec(wfSpecName);
            wfSpecModel.setVersion(wfSpecVersion);
            final StoredGetable<WfSpec, WfSpecModel> storedGetable = new StoredGetable<>(wfSpecModel);
            final String key = storedGetable.getFullStoreKey();
            final Bytes value = Bytes.wrap(storedGetable.toBytes());

            metadataCache.updateCache(key, value);

            StoredGetablePb cachedWfSpecModel = (StoredGetablePb) metadataCache.get(
                    new WfSpecIdModel(wfSpecName, wfSpecVersion).toProto().build());
            WfSpec storedWfSpec = WfSpec.parseFrom(cachedWfSpecModel.getGetablePayload());
            assertThat(storedWfSpec).isEqualTo(wfSpecModel.toProto().build());
        }

        @Test
        public void shouldAddDeserializedTaskDefToCache() throws LHSerdeError, InvalidProtocolBufferException {
            final MetadataCache metadataCache = new MetadataCache();
            final String taskName = "task-something";
            final TaskDefModel taskDef = TestUtil.taskDef(taskName);
            final StoredGetable<TaskDef, TaskDefModel> storedGetable = new StoredGetable<>(taskDef);
            final String key = storedGetable.getFullStoreKey();
            final Bytes value = Bytes.wrap(storedGetable.toBytes());

            metadataCache.updateCache(key, value);

            StoredGetablePb cachedTaskDef = (StoredGetablePb)
                    metadataCache.get(new TaskDefIdModel(taskName).toProto().build());
            TaskDef storedTaskDef = TaskDef.parseFrom(cachedTaskDef.getGetablePayload());
            assertThat(storedTaskDef).isEqualTo(taskDef.toProto().build());
        }

        @Test
        public void shouldAddDeserializedWfSpecWithLatestToCache() throws LHSerdeError, InvalidProtocolBufferException {
            final MetadataCache metadataCache = new MetadataCache();
            final String wfSpecName = "WF1";
            final int wfSpecVersion = 23;
            final WfSpecModel wfSpecModel = TestUtil.wfSpec(wfSpecName);
            wfSpecModel.setVersion(wfSpecVersion);
            final StoredGetable<WfSpec, WfSpecModel> storedGetable = new StoredGetable<>(wfSpecModel);
            final String key = storedGetable.getFullStoreKey();
            final Bytes value = Bytes.wrap(storedGetable.toBytes());

            metadataCache.updateCache(key, value);

            StoredGetablePb cachedLatestWfSpecModel = (StoredGetablePb) metadataCache.get(
                    new WfSpecIdModel(wfSpecName, LATEST_VERSION).toProto().build());
            WfSpec storedWfSpec = WfSpec.parseFrom(cachedLatestWfSpecModel.getGetablePayload());
            assertThat(storedWfSpec).isEqualTo(wfSpecModel.toProto().build());
        }

        @Test
        public void shouldEvictWfSpecWithVersionFromCacheWhenValueIsNull() throws LHSerdeError {
            final MetadataCache metadataCache = new MetadataCache();
            final String key = "0/2/WF1/23";
            final WfSpecIdModel cacheKey = new WfSpecIdModel("WF1", 23);
            final Bytes value = null;

            metadataCache.updateCache(
                    cacheKey.toProto().build(), TestUtil.wfSpec("WF1").toProto().build());

            metadataCache.updateCache(key, value);

            assertThat(metadataCache.get(cacheKey.toProto().build())).isNull();
        }

        @Test
        public void shouldEvictTaskDefFromCacheWhenValueIsNull() throws LHSerdeError {
            final MetadataCache metadataCache = new MetadataCache();
            final String key = "0/0/task-something";
            final TaskDefIdModel cacheKey = new TaskDefIdModel("task-something");
            final Bytes value = null;

            metadataCache.updateCache(
                    cacheKey.toProto().build(),
                    TestUtil.taskDef("task-something").toProto().build());

            metadataCache.updateCache(key, value);

            assertThat(metadataCache.get(cacheKey.toProto().build())).isNull();
        }

        @Test
        public void shouldEvictLatestWfSpecFromCacheWhenValueIsNull() throws LHSerdeError {
            final MetadataCache metadataCache = new MetadataCache();
            final String key = "0/2/WF1/23";
            final WfSpecIdModel latestCacheKey = new WfSpecIdModel("WF1", LATEST_VERSION);
            final Bytes value = null;

            metadataCache.updateCache(
                    latestCacheKey.toProto().build(),
                    TestUtil.wfSpec("WF1").toProto().build());

            metadataCache.updateCache(key, value);

            assertThat(metadataCache.get(latestCacheKey.toProto().build())).isNull();
        }

        @Test
        public void shouldNotCacheKeysThatAreNotWfSpec() throws LHSerdeError {
            final MetadataCache metadataCache = new MetadataCache();
            final String nonWfSpecKey = "0/11/WF1/123/0b80d81e-8984-4da5-8312-f19e3fbfa780";
            final WfSpecIdModel cacheKey = new WfSpecIdModel("WF1", 23);
            final Bytes value = Bytes.wrap(TestUtil.taskRun().toBytes());

            metadataCache.updateCache(nonWfSpecKey, value);

            assertThat(metadataCache.get(cacheKey.toProto().build())).isNull();
        }
    }

    @Nested
    class GetOrCache {

        @Test
        public void shouldNotCacheNullValues() {
            final MetadataCache metadataCache = new MetadataCache();
            String name = "WF1";
            int version = 23;
            final WfSpecIdModel cacheKey = new WfSpecIdModel(name, version);

            metadataCache.getOrCache(cacheKey.toProto().build(), () -> null);
            metadataCache.getOrCache(name, version, () -> null);

            assertThat(metadataCache.get(cacheKey.toProto().build())).isNull();
        }

        @Test
        public void shouldCreateLatestVersionWhenVersionIsNull() {
            final MetadataCache metadataCache = new MetadataCache();
            String name = "WF1";
            Integer version = null;
            WfSpecModel wfSpecModel = TestUtil.wfSpec(name);

            metadataCache.getOrCache(name, version, () -> wfSpecModel.toProto().build());

            WfSpec cachedLatestWfSpecModel = (WfSpec) metadataCache.get(
                    new WfSpecIdModel(name, LATEST_VERSION).toProto().build());

            assertThat(cachedLatestWfSpecModel)
                    .usingRecursiveComparison()
                    .ignoringFields("threadSpecs")
                    .isEqualTo(wfSpecModel.toProto().build());
        }
    }
}

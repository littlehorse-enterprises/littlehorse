package io.littlehorse.server.streams.topology.core;

import static org.assertj.core.api.Assertions.*;

import io.littlehorse.TestUtil;
import io.littlehorse.common.model.getable.global.taskdef.TaskDefModel;
import io.littlehorse.server.TestMetadataManager;
import io.littlehorse.server.TestRequestExecutionContext;
import io.littlehorse.server.streams.util.MetadataCache;
import org.junit.jupiter.api.Test;

public class WfServiceTest {

    private final TestRequestExecutionContext executionContext = TestRequestExecutionContext.create();
    private final WfService service = executionContext.service();
    private final TestMetadataManager metadataManager = TestMetadataManager.create(
            executionContext.getGlobalMetadataNativeStore(),
            executionContext.getTenantId(),
            executionContext,
            new MetadataCache());

    /*@Test
    public void shouldReturnTaskDefFromCache() {
        TaskDefModel taskDef = TestUtil.taskDef("myTask");
        StoredGetable taskDefToStore = new StoredGetable(taskDef);
        executionContext.getMetadataCache().updateCache(taskDefToStore.getStoredObject().getObjectId().toProto().build(), taskDefToStore.toProto().build());
        TaskDefModel result = service.getTaskDef("myTask");
        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(taskDef);
    }*/

    @Test
    public void shouldReturnTaskDefFromStore() {
        TaskDefModel taskDefToStore = TestUtil.taskDef("myTask");
        metadataManager.put(taskDefToStore);
        TaskDefModel result = service.getTaskDef("myTask");
        assertThat(result).usingRecursiveComparison().isEqualTo(taskDefToStore);
    }
}

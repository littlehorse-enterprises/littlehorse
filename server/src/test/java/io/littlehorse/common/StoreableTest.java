package io.littlehorse.common;

import static org.assertj.core.api.Assertions.*;

import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.StoreableType;
import org.junit.jupiter.api.Test;

class StoreableTest {

    @Test
    void shouldGenerateGroupedFullStoreKey() {
        WfRunIdModel wfRunId = new WfRunIdModel("test-wf-run-123");
        StoreableType storeType = StoreableType.STORED_GETABLE;
        GetableClassEnum getableType = GetableClassEnum.NODE_RUN;
        String storeKey = "0/1";

        String result = Storeable.getGroupedFullStoreKey(wfRunId, storeType, getableType, storeKey);

        String expected = Storeable.GROUPED_WF_RUN_PREFIX + "/" + wfRunId + "/" + storeType.getNumber() + "/"
                + getableType.getNumber() + "/" + storeKey;
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void shouldGenerateGroupedGetableStorePrefix() {
        String wfRunId = "test-wf-run-456";
        StoreableType storeType = StoreableType.STORED_GETABLE;
        GetableClassEnum getableType = GetableClassEnum.TASK_RUN;

        String result = Storeable.getGroupedGetableStorePrefix(wfRunId, storeType, getableType);

        String expected = Storeable.GROUPED_WF_RUN_PREFIX + "/" + wfRunId + "/" + storeType.getNumber() + "/"
                + getableType.getNumber() + "/";
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void shouldGenerateGroupedGetableStorePrefixWithRestOfPrefix() {
        String wfRunId = "test-wf-run-789";
        StoreableType storeType = StoreableType.STORED_GETABLE;
        GetableClassEnum getableType = GetableClassEnum.WF_RUN;
        String restOfPrefix = "extra/path";

        String result = Storeable.getGroupedGetableStorePrefix(wfRunId, storeType, getableType, restOfPrefix);

        String expected = Storeable.GROUPED_WF_RUN_PREFIX + "/" + wfRunId + "/" + storeType.getNumber() + "/"
                + getableType.getNumber() + "/" + restOfPrefix + "/";
        assertThat(result).isEqualTo(expected);
    }

}

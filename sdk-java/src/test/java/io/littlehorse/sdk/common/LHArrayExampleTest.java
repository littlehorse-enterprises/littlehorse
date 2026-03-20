package io.littlehorse.sdk.common;

import static org.junit.jupiter.api.Assertions.*;

import io.littlehorse.sdk.common.proto.VariableValue;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

public class LHArrayExampleTest {

    @Test
    public void exampleLHArraySerializationRoundtrip() throws Exception {
        LHArray<Long> arr = LHArray.of(Arrays.asList(10L, 20L));

        VariableValue vv = LHLibUtil.objToVarVal(arr);
        assertEquals(VariableValue.ValueCase.ARRAY, vv.getValueCase());

        Object des = LHLibUtil.varValToObj(vv, LHArray.class);
        assertTrue(des instanceof LHArray);

        LHArray<?> darr = (LHArray<?>) des;
        List<?> list = darr.asList();
        assertEquals(2, list.size());
        assertEquals(10L, ((Number) list.get(0)).longValue());
        assertEquals(20L, ((Number) list.get(1)).longValue());
    }
}

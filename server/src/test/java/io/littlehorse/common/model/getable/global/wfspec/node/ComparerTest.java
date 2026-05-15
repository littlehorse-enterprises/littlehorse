package io.littlehorse.common.model.getable.global.wfspec.node;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.sdk.common.proto.Array;
import io.littlehorse.sdk.common.proto.VariableValue;
import org.junit.jupiter.api.Test;

public class ComparerTest {

    @Test
    public void testArrayContainsLiteralInt() throws Exception {
        // Build a VariableValue proto representing an ARRAY of INTs [1,2,3]
        Array.Builder arr = Array.newBuilder();
        arr.addItems(VariableValue.newBuilder().setInt(1L).build());
        arr.addItems(VariableValue.newBuilder().setInt(2L).build());
        arr.addItems(VariableValue.newBuilder().setInt(3L).build());

        VariableValue leftProto = VariableValue.newBuilder().setArray(arr).build();
        VariableValue rightProto = VariableValue.newBuilder().setInt(2L).build();

        VariableValueModel left = VariableValueModel.fromProto(leftProto, null);
        VariableValueModel right = VariableValueModel.fromProto(rightProto, null);

        assertTrue(Comparer.contains(left, right));
    }
}

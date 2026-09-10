package io.littlehorse.common.model.getable.global.wfspec.node;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.sdk.common.proto.Array;
import io.littlehorse.sdk.common.proto.Map;
import io.littlehorse.sdk.common.proto.VariableValue;
import java.util.Arrays;
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

    @Test
    public void shouldConsiderMapsWithReorderedEntriesEqual() throws Exception {
        VariableValueModel left =
                mapModelOf(mapEntry(stringValue("first"), intValue(1)), mapEntry(stringValue("second"), intValue(2)));
        VariableValueModel right =
                mapModelOf(mapEntry(stringValue("second"), intValue(2)), mapEntry(stringValue("first"), intValue(1)));

        assertEquals(0, Comparer.compare(left, right));
    }

    @Test
    public void shouldConsiderMapsWithDifferentContentsUnequal() throws Exception {
        VariableValueModel left =
                mapModelOf(mapEntry(stringValue("first"), intValue(1)), mapEntry(stringValue("second"), intValue(2)));
        VariableValueModel right =
                mapModelOf(mapEntry(stringValue("second"), intValue(3)), mapEntry(stringValue("first"), intValue(1)));

        assertNotEquals(0, Comparer.compare(left, right));
    }

    @Test
    public void shouldCompareMapsWithNullValues() throws Exception {
        VariableValueModel left = mapModelOf(mapEntry(stringValue("optional"), nullValue()));
        VariableValueModel equalRight = mapModelOf(mapEntry(stringValue("optional"), nullValue()));
        VariableValueModel unequalRight = mapModelOf(mapEntry(stringValue("optional"), intValue(1)));

        assertEquals(0, Comparer.compare(left, equalRight));
        assertNotEquals(0, Comparer.compare(left, unequalRight));
    }

    @Test
    public void shouldConsiderNestedMapsWithReorderedEntriesEqual() throws Exception {
        VariableValueModel left = mapModelOf(
                mapEntry(
                        stringValue("outer-a"),
                        mapValueOf(
                                mapEntry(stringValue("inner-a"), intValue(1)),
                                mapEntry(stringValue("inner-b"), intValue(2)))),
                mapEntry(stringValue("outer-b"), mapValueOf(mapEntry(stringValue("inner-c"), intValue(3)))));
        VariableValueModel right = mapModelOf(
                mapEntry(stringValue("outer-b"), mapValueOf(mapEntry(stringValue("inner-c"), intValue(3)))),
                mapEntry(
                        stringValue("outer-a"),
                        mapValueOf(
                                mapEntry(stringValue("inner-b"), intValue(2)),
                                mapEntry(stringValue("inner-a"), intValue(1)))));

        assertEquals(0, Comparer.compare(left, right));
    }

    private static VariableValueModel mapModelOf(Map.Entry... entries) throws Exception {
        return VariableValueModel.fromProto(mapValueOf(entries), null);
    }

    private static VariableValue mapValueOf(Map.Entry... entries) {
        return VariableValue.newBuilder()
                .setMap(Map.newBuilder().addAllEntries(Arrays.asList(entries)))
                .build();
    }

    private static Map.Entry mapEntry(VariableValue key, VariableValue value) {
        return Map.Entry.newBuilder().setKey(key).setValue(value).build();
    }

    private static VariableValue stringValue(String value) {
        return VariableValue.newBuilder().setStr(value).build();
    }

    private static VariableValue intValue(long value) {
        return VariableValue.newBuilder().setInt(value).build();
    }

    private static VariableValue nullValue() {
        return VariableValue.newBuilder().build();
    }
}

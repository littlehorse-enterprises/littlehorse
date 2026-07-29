package io.littlehorse.sdkjs.golden;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.common.proto.WfRunId;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Emits how the Java SDK encodes representative values as VariableValue, so
 * sdk-js can assert byte-for-byte agreement instead of assuming it.
 *
 * This is the same trick as the WfSpec goldens, applied one layer down: the
 * serde layer is what every other layer sits on, and two SDKs can each "work"
 * while disagreeing on encoding — which would silently corrupt data written
 * by one and read by the other.
 *
 * Regenerate with:
 *   ./gradlew :sdk-js-golden-generator:runSerde --args="$(pwd)/sdk-js/golden"
 */
public class SerdeGoldenGenerator {

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Usage: SerdeGoldenGenerator <output-dir>");
            System.exit(1);
        }
        Path outputDir = Path.of(args[0]).resolve("fixtures");
        Files.createDirectories(outputDir);

        // Each case is a label the JS side matches on, plus the value.
        Map<String, Object> cases = new LinkedHashMap<>();
        cases.put("str", "hello");
        cases.put("str-empty", "");
        cases.put("str-unicode", "héllo → 世界");
        cases.put("int-long", 42L);
        cases.put("int-zero", 0L);
        cases.put("int-negative", -7L);
        cases.put("int-large", 9007199254740991L);
        cases.put("double", 1.5d);
        cases.put("double-negative", -0.25d);
        cases.put("bool-true", true);
        cases.put("bool-false", false);
        cases.put("bytes", new byte[] {1, 2, 3, (byte) 255});
        cases.put("timestamp-epoch", Instant.ofEpochMilli(0));
        cases.put("timestamp", Instant.ofEpochMilli(1780000000123L));
        cases.put("json-obj", orderedMap());
        cases.put("json-obj-empty", new LinkedHashMap<String, Object>());
        cases.put("json-obj-null-field", nullFieldMap());
        cases.put("json-arr", List.of(1L, 2L, 3L));
        cases.put("json-arr-empty", List.of());
        cases.put("json-arr-mixed", List.of("a", 1L, true));
        cases.put("json-arr-with-null", java.util.Arrays.asList(1L, null, 2L));
        cases.put("json-obj-nested-null", nestedNullMap());
        cases.put("json-obj-all-null", java.util.Collections.singletonMap("only", null));
        cases.put("null", null);

        List<String> entries = new ArrayList<>();
        for (Map.Entry<String, Object> entry : cases.entrySet()) {
            VariableValue value = LHLibUtil.objToVarVal(entry.getValue());
            entries.add(String.format(
                    "    {\"label\": \"%s\", \"encoded\": %s}", entry.getKey(), LHLibUtil.protoToJson(value)));
        }

        // WfRunId / TaskRunId string formats, which are their own convention.
        WfRunId parent = WfRunId.newBuilder().setId("parent-1").build();
        WfRunId child =
                WfRunId.newBuilder().setId("child-1").setParentWfRunId(parent).build();
        WfRunId grandchild =
                WfRunId.newBuilder().setId("gc-1").setParentWfRunId(child).build();

        String ids = String.format(
                "  \"ids\": {\n"
                        + "    \"simple\": \"%s\",\n"
                        + "    \"child\": \"%s\",\n"
                        + "    \"grandchild\": \"%s\",\n"
                        + "    \"taskRun\": \"%s\"\n"
                        + "  }",
                LHLibUtil.wfRunIdToString(parent),
                LHLibUtil.wfRunIdToString(child),
                LHLibUtil.wfRunIdToString(grandchild),
                LHLibUtil.taskRunIdToString(io.littlehorse.sdk.common.proto.TaskRunId.newBuilder()
                        .setWfRunId(child)
                        .setTaskGuid("guid-9")
                        .build()));

        String json = "{\n  \"values\": [\n" + String.join(",\n", entries) + "\n  ],\n" + ids + "\n}\n";
        Path out = outputDir.resolve("serde.json");
        Files.writeString(out, json, StandardCharsets.UTF_8);
        System.out.println("Wrote " + out + " (" + cases.size() + " values)");
    }

    private static Map<String, Object> orderedMap() {
        // Deliberately not alphabetical, so field ordering differences show up.
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("zebra", "last-alphabetically");
        map.put("alpha", 1L);
        map.put("nested", Map.of("inner", true));
        return map;
    }

    private static Map<String, Object> nestedNullMap() {
        Map<String, Object> inner = new LinkedHashMap<>();
        inner.put("kept", 1L);
        inner.put("dropped", null);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("outer", inner);
        return map;
    }

    private static Map<String, Object> nullFieldMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("present", "yes");
        map.put("absent", null);
        return map;
    }
}

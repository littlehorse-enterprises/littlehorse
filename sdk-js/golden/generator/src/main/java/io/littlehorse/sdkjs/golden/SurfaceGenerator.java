package io.littlehorse.sdkjs.golden;

import io.littlehorse.sdk.wfsdk.ExternalEventNodeOutput;
import io.littlehorse.sdk.wfsdk.IfElseBody;
import io.littlehorse.sdk.wfsdk.InlineLHStructBuilder;
import io.littlehorse.sdk.wfsdk.InterruptHandler;
import io.littlehorse.sdk.wfsdk.LHExpression;
import io.littlehorse.sdk.wfsdk.LHFormatString;
import io.littlehorse.sdk.wfsdk.LHStructBuilder;
import io.littlehorse.sdk.wfsdk.NodeOutput;
import io.littlehorse.sdk.wfsdk.SpawnedChildWf;
import io.littlehorse.sdk.wfsdk.SpawnedThread;
import io.littlehorse.sdk.wfsdk.SpawnedThreads;
import io.littlehorse.sdk.wfsdk.TaskNodeOutput;
import io.littlehorse.sdk.wfsdk.ThreadFunc;
import io.littlehorse.sdk.wfsdk.ThrowEventNodeOutput;
import io.littlehorse.sdk.wfsdk.UserTaskOutput;
import io.littlehorse.sdk.wfsdk.WaitForConditionNodeOutput;
import io.littlehorse.sdk.wfsdk.WaitForThreadsNodeOutput;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.WorkflowIfStatement;
import io.littlehorse.sdk.wfsdk.WorkflowThread;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;

/**
 * Emits the public wfsdk API surface of sdk-java as java-surface.json — the
 * denominator the sdk-js feature matrix is checked against (the "freshness
 * check"; see proposals/sdk-js-parity/wfsdk.md, Design 1).
 *
 * The output is deterministic (sorted classes, sorted methods) so the file is
 * stable across runs and any change to it is a real change in sdk-java's
 * public surface. Overloads collapse to one entry per method name — the JS SDK
 * collapses them too — with the overload count kept for the human report.
 *
 * Regenerate with:
 *   ./gradlew :sdk-js-golden-generator:runSurface --args="$(pwd)/sdk-js/golden"
 */
public class SurfaceGenerator {

    /**
     * The public wfsdk types (everything in io.littlehorse.sdk.wfsdk except
     * internal/). Listed explicitly: reflection cannot scan a package, and an
     * explicit list means adding a NEW public type to sdk-java without adding
     * it here is visible in review rather than silently uncounted.
     */
    private static final Class<?>[] COVERED_CLASSES = {
        ExternalEventNodeOutput.class,
        IfElseBody.class,
        InlineLHStructBuilder.class,
        InterruptHandler.class,
        LHExpression.class,
        LHFormatString.class,
        LHStructBuilder.class,
        NodeOutput.class,
        SpawnedChildWf.class,
        SpawnedThread.class,
        SpawnedThreads.class,
        TaskNodeOutput.class,
        ThreadFunc.class,
        ThrowEventNodeOutput.class,
        UserTaskOutput.class,
        WaitForConditionNodeOutput.class,
        WaitForThreadsNodeOutput.class,
        WfRunVariable.class,
        Workflow.class,
        WorkflowIfStatement.class,
        WorkflowThread.class,
    };

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Usage: SurfaceGenerator <output-dir>");
            System.exit(1);
        }
        // Lives in fixtures/ (like serde.json): golden/*.json at the top level
        // is reserved for compiled workflows, which the golden self-test
        // parses as PutWfSpecRequests.
        Path outputDir = Path.of(args[0]).resolve("fixtures");
        Files.createDirectories(outputDir);

        // class name -> method name -> [overloads, deprecated]
        Map<String, Map<String, int[]>> surface = new TreeMap<>();
        int methodCount = 0;

        for (Class<?> type : COVERED_CLASSES) {
            Map<String, int[]> methods = new TreeMap<>();
            for (Method m : type.getDeclaredMethods()) {
                if (!Modifier.isPublic(m.getModifiers())) continue;
                if (m.isSynthetic() || m.isBridge()) continue;
                boolean deprecated = m.isAnnotationPresent(Deprecated.class);
                int[] entry = methods.computeIfAbsent(m.getName(), k -> new int[] {0, 0});
                entry[0]++;
                if (deprecated) entry[1] = 1;
            }
            surface.put(type.getSimpleName(), methods);
            methodCount += methods.size();
        }

        StringBuilder json = new StringBuilder();
        json.append("{\n  \"types\": {\n");
        boolean firstType = true;
        for (Map.Entry<String, Map<String, int[]>> type : surface.entrySet()) {
            if (!firstType) json.append(",\n");
            firstType = false;
            json.append("    \"").append(type.getKey()).append("\": {");
            boolean firstMethod = true;
            for (Map.Entry<String, int[]> m : type.getValue().entrySet()) {
                if (!firstMethod) json.append(",");
                firstMethod = false;
                json.append("\n      \"")
                        .append(m.getKey())
                        .append("\": { \"overloads\": ")
                        .append(m.getValue()[0])
                        .append(", \"deprecated\": ")
                        .append(m.getValue()[1] == 1)
                        .append(" }");
            }
            json.append(firstMethod ? "}" : "\n    }");
        }
        json.append("\n  }\n}\n");

        Path out = outputDir.resolve("java-surface.json");
        Files.writeString(out, json.toString(), StandardCharsets.UTF_8);
        System.out.println("Wrote " + out + " (" + surface.size() + " types, " + methodCount + " distinct methods)");
    }
}

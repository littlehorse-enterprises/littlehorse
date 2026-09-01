package io.littlehorse.sdk.conformance;

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
import java.util.TreeSet;
import java.util.function.Function;

/**
 * Canon path: writes surface.json + fixtures from WfsdkArea's definitions,
 * never its own. Run only inside a PR (rules.md, "Minting").
 */
public final class WfsdkAreaMint {

    /**
     * The public wfsdk types (everything in io.littlehorse.sdk.wfsdk except
     * internal/). Listed explicitly: reflection cannot scan a package, and an
     * explicit list means a NEW public type is visible in review rather than
     * silently uncounted.
     */
    private static final Class<?>[] SURFACE_CLASSES = {
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

    private WfsdkAreaMint() {}

    static void mint(Path wfsdkDir) throws Exception {
        // sorted so the file is stable and any diff is a real surface change;
        // overloads collapse to one capability per method name
        TreeSet<String> capabilities = new TreeSet<>();
        for (Class<?> type : SURFACE_CLASSES) {
            for (Method m : type.getDeclaredMethods()) {
                if (!Modifier.isPublic(m.getModifiers())) continue;
                if (m.isSynthetic() || m.isBridge()) continue;
                capabilities.add(type.getSimpleName() + "#" + m.getName());
            }
        }

        StringBuilder surface = new StringBuilder("{\n  \"capabilities\": [\n");
        int i = 0;
        for (String capability : capabilities) {
            surface.append("    \"")
                    .append(capability)
                    .append("\"")
                    .append(++i < capabilities.size() ? "," : "")
                    .append("\n");
        }
        surface.append("  ]\n}\n");
        Files.writeString(wfsdkDir.resolve("surface.json"), surface.toString(), StandardCharsets.UTF_8);

        for (Map.Entry<String, Workflow> entry : WfsdkArea.singles().entrySet()) {
            Path caseDir = Files.createDirectories(wfsdkDir.resolve("cases").resolve(entry.getKey()));
            Files.writeString(
                    caseDir.resolve("feature.json"), entry.getValue().compileWfToJson() + "\n", StandardCharsets.UTF_8);
        }
        for (Map.Entry<String, Function<Boolean, Workflow>> entry :
                WfsdkArea.pairs().entrySet()) {
            Path caseDir = Files.createDirectories(wfsdkDir.resolve("cases").resolve(entry.getKey()));
            Files.writeString(
                    caseDir.resolve("base.json"),
                    entry.getValue().apply(false).compileWfToJson() + "\n",
                    StandardCharsets.UTF_8);
            Files.writeString(
                    caseDir.resolve("feature.json"),
                    entry.getValue().apply(true).compileWfToJson() + "\n",
                    StandardCharsets.UTF_8);
        }
        System.out.println("Minted wfsdk: surface.json (" + capabilities.size() + " capabilities), "
                + (WfsdkArea.singles().size() + WfsdkArea.pairs().size()) + " case(s)");
    }
}

package io.littlehorse.sdk.conformance;

import io.littlehorse.sdk.wfsdk.ThreadFunc;
import io.littlehorse.sdk.wfsdk.Workflow;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * wfsdk case definitions + the exam's `compile` answers. WfsdkAreaMint
 * mints from these same definitions, so answers and canon cannot drift.
 * Recipe: conformance/areas/wfsdk/rules.md.
 */
public final class WfsdkArea {

    /** Pair cases: compile(false) = base variant, compile(true) = feature variant. */
    private static final Map<String, Function<Boolean, Workflow>> PAIRS = new LinkedHashMap<>();

    /**
     * Single-variant: capabilities that are the precondition of any output,
     * so no base exists (proposals/sdk-conformance/design.md).
     */
    private static final Map<String, Workflow> SINGLES = new LinkedHashMap<>();

    /** The reference capabilities this area's corpus must cover. */
    private static final Set<String> SURFACE = new LinkedHashSet<>();

    private WfsdkArea() {}

    private static void pair(String caseId, String capability, Function<Boolean, ThreadFunc> body) {
        PAIRS.put(caseId, withFeature -> Workflow.newWorkflow("probe-" + caseId, body.apply(withFeature)));
        SURFACE.add(capability);
    }

    private static void single(String caseId, List<String> capabilities, ThreadFunc body) {
        SINGLES.put(caseId, Workflow.newWorkflow("probe-" + caseId, body));
        SURFACE.addAll(capabilities);
    }

    static {
        single("workflow-minimal", List.of("Workflow#newWorkflow", "Workflow#compileWorkflow"), wf -> {});

        pair("sleep-seconds", "WorkflowThread#sleepSeconds", f -> wf -> {
            if (f) wf.sleepSeconds(30);
        });
    }

    static Set<String> caseIds() {
        Set<String> ids = new LinkedHashSet<>(SINGLES.keySet());
        ids.addAll(PAIRS.keySet());
        return ids;
    }

    static String compile(String caseId, String variant) {
        if (SINGLES.containsKey(caseId)) {
            if (!variant.equals("feature")) {
                throw new IllegalArgumentException("case " + caseId + " is single-variant; only --variant feature exists");
            }
            return SINGLES.get(caseId).compileWfToJson();
        }
        Function<Boolean, Workflow> builder = PAIRS.get(caseId);
        if (builder == null) {
            throw new IllegalArgumentException("unknown case: " + caseId);
        }
        boolean withFeature =
                switch (variant) {
                    case "base" -> false;
                    case "feature" -> true;
                    default -> throw new IllegalArgumentException("variant must be base|feature: " + variant);
                };
        return builder.apply(withFeature).compileWfToJson();
    }

    static Map<String, Function<Boolean, Workflow>> pairs() {
        return PAIRS;
    }

    static Map<String, Workflow> singles() {
        return SINGLES;
    }

    static Set<String> surface() {
        return SURFACE;
    }
}

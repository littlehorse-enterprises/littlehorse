package io.littlehorse.sdk.conformance;

import io.littlehorse.sdk.wfsdk.ThreadFunc;
import io.littlehorse.sdk.wfsdk.Workflow;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * sdk-java's conformance testee (see conformance/README.md for the contract):
 *
 *   list                                     print implemented case ids
 *   compile --case ID --variant base|feature print the compiled proto JSON
 *   mint DIR                                 write DIR/surface.json and
 *                                            DIR/cases/ID/{base,feature}.json
 *
 * `compile` is the exam path: sdk-java answers like any other SDK. `mint` is
 * the canon path: it regenerates the frozen fixtures, and is only ever run
 * inside a PR where the fixture diff is the review surface.
 */
public class ConformanceTestee {

    /** Pair cases: compile(false) = base variant, compile(true) = feature variant. */
    private static final Map<String, Function<Boolean, Workflow>> PAIRS = new LinkedHashMap<>();

    /**
     * Single-variant cases (one `feature` fixture, no base) — only for
     * capabilities that are the precondition of any output at all and so
     * cannot be toggled off (see proposals/sdk-conformance/design.md).
     */
    private static final Map<String, Workflow> SINGLES = new LinkedHashMap<>();

    /** The reference capabilities the corpus must cover (grown alongside the cases). */
    private static final Set<String> SURFACE = new LinkedHashSet<>();

    private static void pair(String caseId, String capability, Function<Boolean, ThreadFunc> body) {
        PAIRS.put(caseId, withFeature -> Workflow.newWorkflow("probe-" + caseId, body.apply(withFeature)));
        SURFACE.add(capability);
    }

    private static void single(String caseId, List<String> capabilities, ThreadFunc body) {
        SINGLES.put(caseId, Workflow.newWorkflow("probe-" + caseId, body));
        SURFACE.addAll(capabilities);
    }

    static {
        // Creating and compiling a workflow is what every other case stands
        // on; toggling it off yields no output at all, so this ships as the
        // corpus's one single-variant case.
        single("workflow-minimal", List.of("Workflow#newWorkflow", "Workflow#compileWorkflow"), wf -> {});

        pair("sleep-seconds", "WorkflowThread#sleepSeconds", f -> wf -> {
            if (f) wf.sleepSeconds(30);
        });
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 1 && args[0].equals("list")) {
            SINGLES.keySet().forEach(System.out::println);
            PAIRS.keySet().forEach(System.out::println);
            return;
        }
        if (args.length == 5 && args[0].equals("compile") && args[1].equals("--case") && args[3].equals("--variant")) {
            String caseId = args[2];
            String variant = args[4];
            if (SINGLES.containsKey(caseId)) {
                if (!variant.equals("feature")) {
                    System.err.println("case " + caseId + " is single-variant; only --variant feature exists");
                    System.exit(2);
                }
                System.out.println(SINGLES.get(caseId).compileWfToJson());
                return;
            }
            Function<Boolean, Workflow> builder = PAIRS.get(caseId);
            if (builder == null) {
                System.err.println("unknown case: " + caseId);
                System.exit(2);
            }
            boolean withFeature =
                    switch (variant) {
                        case "base" -> false;
                        case "feature" -> true;
                        default -> throw new IllegalArgumentException("variant must be base|feature: " + variant);
                    };
            System.out.println(builder.apply(withFeature).compileWfToJson());
            return;
        }
        if (args.length == 2 && args[0].equals("mint")) {
            mint(Path.of(args[1]));
            return;
        }
        System.err.println("usage: testee list | compile --case ID --variant base|feature | mint DIR");
        System.exit(2);
    }

    private static void mint(Path wfsdkDir) throws Exception {
        // Every surface capability is verified against the real class by
        // reflection, so a typo'd key cannot become canon.
        StringBuilder surface = new StringBuilder();
        surface.append("{\n  \"capabilities\": [\n");
        int i = 0;
        for (String capability : SURFACE) {
            String[] parts = capability.split("#");
            Class<?> clazz = Class.forName("io.littlehorse.sdk.wfsdk." + parts[0]);
            boolean exists = false;
            for (Method m : clazz.getMethods()) {
                if (m.getName().equals(parts[1])) exists = true;
            }
            if (!exists) throw new IllegalStateException("surface capability does not exist: " + capability);
            surface.append("    \"")
                    .append(capability)
                    .append("\"")
                    .append(++i < SURFACE.size() ? "," : "")
                    .append("\n");
        }
        surface.append("  ]\n}\n");
        Files.writeString(wfsdkDir.resolve("surface.json"), surface.toString(), StandardCharsets.UTF_8);

        for (Map.Entry<String, Workflow> entry : SINGLES.entrySet()) {
            Path caseDir = Files.createDirectories(wfsdkDir.resolve("cases").resolve(entry.getKey()));
            Files.writeString(
                    caseDir.resolve("feature.json"),
                    entry.getValue().compileWfToJson() + "\n",
                    StandardCharsets.UTF_8);
        }
        for (Map.Entry<String, Function<Boolean, Workflow>> entry : PAIRS.entrySet()) {
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
        System.out.println("Minted surface.json (" + SURFACE.size() + " capabilities) and "
                + (SINGLES.size() + PAIRS.size()) + " case(s) into " + wfsdkDir);
    }
}

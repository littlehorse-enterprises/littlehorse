package io.littlehorse.sdk.conformance;

import com.google.gson.Gson;
import io.littlehorse.sdk.common.proto.CorrelatedEventConfig;
import io.littlehorse.sdk.common.proto.PutExternalEventDefRequest;
import io.littlehorse.sdk.common.proto.PutWorkflowEventDefRequest;
import io.littlehorse.sdk.wfsdk.ThreadFunc;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;

/**
 * registrations case definitions + the exam's `registrations` answers: the
 * side-registration protos and required-names sets a workflow produces
 * besides its WfSpec. RegistrationsAreaMint mints from these same
 * definitions. Recipe: sdk-conformance/areas/registrations/rules.md.
 */
public final class RegistrationsArea {

    /** Pair cases: answer(false) = base variant, answer(true) = feature variant. */
    private static final Map<String, Function<Boolean, Workflow>> PAIRS = new LinkedHashMap<>();

    private RegistrationsArea() {}

    private static void pair(String caseId, Function<Boolean, ThreadFunc> body) {
        PAIRS.put(caseId, withFeature -> Workflow.newWorkflow("probe-" + caseId, body.apply(withFeature)));
    }

    static {
        pair("reg-external-event", f -> wf -> {
            var evt = wf.waitForEvent("payment-received");
            if (f) evt.registeredAs(String.class);
        });
        pair("reg-correlated-event-config", f -> wf -> {
            var evt = wf.waitForEvent("payment-received").registeredAs(String.class);
            if (f) {
                evt.withCorrelatedEventConfig(CorrelatedEventConfig.newBuilder()
                        .setTtlSeconds(3600)
                        .setDeleteAfterFirstCorrelation(true)
                        .build());
            }
        });
        pair("reg-workflow-event", f -> wf -> {
            WfRunVariable payload = wf.declareStr("payload");
            var evt = wf.throwEvent("milestone", payload);
            if (f) evt.registeredAs(String.class);
        });
        pair("reg-interrupt-event-type", f -> wf -> {
            wf.execute("main-step");
            var handler = wf.registerInterruptHandler("cancel-requested", h -> {
                h.execute("cancel");
            });
            if (f) handler.withEventType(String.class);
        });
        pair("req-task-def-names", f -> wf -> {
            wf.declareStr("v");
            if (f) wf.execute("noop");
        });
        pair("req-external-event-def-names", f -> wf -> {
            wf.execute("noop");
            if (f) wf.waitForEvent("payment-received");
        });
        pair("req-child-wf-spec-names", f -> wf -> {
            wf.execute("noop");
            if (f) wf.runWf("child-wf", Map.of());
        });
        pair("req-workflow-event-def-names", f -> wf -> {
            WfRunVariable payload = wf.declareStr("payload");
            wf.execute("noop");
            if (f) wf.throwEvent("milestone", payload);
        });
    }

    static Set<String> caseIds() {
        return PAIRS.keySet();
    }

    static String answer(String caseId, String variant) throws Exception {
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
        Workflow w = builder.apply(withFeature);
        // registrations hydrate during compilation (Java compiles before
        // sending them, for the same reason)
        w.compileWorkflow();

        var printer = com.google.protobuf.util.JsonFormat.printer().includingDefaultValueFields();
        List<PutExternalEventDefRequest> extDefs = new ArrayList<>(w.getExternalEventDefsToRegister());
        extDefs.sort(Comparator.comparing(PutExternalEventDefRequest::getName));
        List<PutWorkflowEventDefRequest> wfDefs = new ArrayList<>(w.getWorkflowEventDefsToRegister());
        wfDefs.sort(Comparator.comparing(PutWorkflowEventDefRequest::getName));

        Gson gson = new Gson();
        StringBuilder out = new StringBuilder("{\n  \"externalEventDefs\": [");
        for (int i = 0; i < extDefs.size(); i++) {
            out.append(i == 0 ? "\n" : ",\n").append(printer.print(extDefs.get(i)));
        }
        out.append(extDefs.isEmpty() ? "" : "\n").append("  ],\n  \"workflowEventDefs\": [");
        for (int i = 0; i < wfDefs.size(); i++) {
            out.append(i == 0 ? "\n" : ",\n").append(printer.print(wfDefs.get(i)));
        }
        out.append(wfDefs.isEmpty() ? "" : "\n").append("  ],\n");
        out.append("  \"requiredTaskDefNames\": ")
                .append(gson.toJson(new TreeSet<>(w.getRequiredTaskDefNames())))
                .append(",\n");
        out.append("  \"requiredExternalEventDefNames\": ")
                .append(gson.toJson(new TreeSet<>(w.getRequiredExternalEventDefNames())))
                .append(",\n");
        out.append("  \"requiredChildWfSpecNames\": ")
                .append(gson.toJson(new TreeSet<>(w.getRequiredChildWfSpecNames())))
                .append(",\n");
        out.append("  \"requiredWorkflowEventDefNames\": ")
                .append(gson.toJson(new TreeSet<>(w.getRequiredWorkflowEventDefNames())))
                .append("\n}");
        return out.toString();
    }
}

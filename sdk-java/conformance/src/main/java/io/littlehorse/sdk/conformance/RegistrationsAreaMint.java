package io.littlehorse.sdk.conformance;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Canon path: writes surface.json + fixtures from RegistrationsArea's
 * definitions, never its own. Run only inside a PR (rules.md, "Minting").
 */
public final class RegistrationsAreaMint {

    /** The capabilities whose observable output is the registrations document. */
    private static final String[] SURFACE = {
        "ExternalEventNodeOutput#registeredAs",
        "ExternalEventNodeOutput#withCorrelatedEventConfig",
        "ThrowEventNodeOutput#registeredAs",
        "InterruptHandler#withEventType",
        "Workflow#getExternalEventDefsToRegister",
        "Workflow#getWorkflowEventDefsToRegister",
        "Workflow#getRequiredTaskDefNames",
        "Workflow#getRequiredExternalEventDefNames",
        "Workflow#getRequiredChildWfSpecNames",
        "Workflow#getRequiredWorkflowEventDefNames",
    };

    private RegistrationsAreaMint() {}

    static void mint(Path areaDir) throws Exception {
        // verified by reflection so a typo'd key cannot become canon
        Set<String> capabilities = new LinkedHashSet<>();
        for (String capability : SURFACE) {
            String[] parts = capability.split("#");
            Class<?> clazz = Class.forName("io.littlehorse.sdk.wfsdk." + parts[0]);
            boolean exists = false;
            for (Method m : clazz.getMethods()) {
                if (m.getName().equals(parts[1])) exists = true;
            }
            if (!exists) throw new IllegalStateException("surface capability does not exist: " + capability);
            capabilities.add(capability);
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
        Files.writeString(areaDir.resolve("surface.json"), surface.toString(), StandardCharsets.UTF_8);

        for (String caseId : RegistrationsArea.caseIds()) {
            Path caseDir = Files.createDirectories(areaDir.resolve("cases").resolve(caseId));
            Files.writeString(
                    caseDir.resolve("base.json"),
                    RegistrationsArea.answer(caseId, "base") + "\n",
                    StandardCharsets.UTF_8);
            Files.writeString(
                    caseDir.resolve("feature.json"),
                    RegistrationsArea.answer(caseId, "feature") + "\n",
                    StandardCharsets.UTF_8);
        }
        System.out.println("Minted registrations: surface.json (" + capabilities.size() + " capabilities), "
                + RegistrationsArea.caseIds().size() + " case(s)");
    }
}

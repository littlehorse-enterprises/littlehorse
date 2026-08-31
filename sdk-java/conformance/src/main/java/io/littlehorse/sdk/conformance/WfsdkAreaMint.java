package io.littlehorse.sdk.conformance;

import io.littlehorse.sdk.wfsdk.Workflow;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Function;

/**
 * Canon path: writes surface.json + fixtures from WfsdkArea's definitions,
 * never its own. Run only inside a PR (rules.md, "Minting").
 */
public final class WfsdkAreaMint {

    private WfsdkAreaMint() {}

    static void mint(Path wfsdkDir) throws Exception {
        // verified by reflection so a typo'd key cannot become canon
        StringBuilder surface = new StringBuilder();
        surface.append("{\n  \"capabilities\": [\n");
        int i = 0;
        for (String capability : WfsdkArea.surface()) {
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
                    .append(++i < WfsdkArea.surface().size() ? "," : "")
                    .append("\n");
        }
        surface.append("  ]\n}\n");
        Files.writeString(wfsdkDir.resolve("surface.json"), surface.toString(), StandardCharsets.UTF_8);

        for (Map.Entry<String, Workflow> entry : WfsdkArea.singles().entrySet()) {
            Path caseDir = Files.createDirectories(wfsdkDir.resolve("cases").resolve(entry.getKey()));
            Files.writeString(
                    caseDir.resolve("feature.json"),
                    entry.getValue().compileWfToJson() + "\n",
                    StandardCharsets.UTF_8);
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
        System.out.println("Minted wfsdk: surface.json (" + WfsdkArea.surface().size() + " capabilities), "
                + (WfsdkArea.singles().size() + WfsdkArea.pairs().size()) + " case(s)");
    }
}

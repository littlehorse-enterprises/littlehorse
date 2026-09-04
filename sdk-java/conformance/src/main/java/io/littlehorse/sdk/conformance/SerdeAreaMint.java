package io.littlehorse.sdk.conformance;

import com.google.gson.Gson;
import io.littlehorse.sdk.common.proto.VariableValue;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Canon path: writes surface.json, manifest, and fixtures from SerdeArea's
 * definitions, never its own (rules.md S2). Run only inside a PR
 * (rules.md, "Minting").
 */
public final class SerdeAreaMint {

    private SerdeAreaMint() {}

    static void mint(Path serdeDir) throws Exception {
        Files.createDirectories(serdeDir.resolve("cases"));

        // the proto is the denominator: a new arm reddens freshness (rules.md S3)
        StringBuilder arms = new StringBuilder("{\n  \"arms\": [\n");
        VariableValue.ValueCase[] cases = VariableValue.ValueCase.values();
        int n = 0;
        for (VariableValue.ValueCase c : cases) {
            if (c == VariableValue.ValueCase.VALUE_NOT_SET) continue;
            n++;
        }
        int written = 0;
        for (VariableValue.ValueCase c : cases) {
            if (c == VariableValue.ValueCase.VALUE_NOT_SET) continue;
            arms.append("    \"").append(lowerCamel(c.name())).append("\"");
            arms.append(++written < n ? "," : "").append("\n");
        }
        arms.append("  ]\n}\n");
        Files.writeString(serdeDir.resolve("surface.json"), arms.toString(), StandardCharsets.UTF_8);

        StringBuilder manifest = new StringBuilder("{\n  \"cases\": [\n");
        int i = 0;
        Gson gson = new Gson();
        for (Map.Entry<String, String[]> entry : SerdeArea.cases().entrySet()) {
            String id = entry.getKey();
            String type = entry.getValue()[0];
            String value = entry.getValue()[1];
            manifest.append("    {\"id\": ")
                    .append(gson.toJson(id))
                    .append(", \"level\": \"required\", \"input\": {\"type\": ")
                    .append(gson.toJson(type));
            if (value != null) manifest.append(", \"value\": ").append(gson.toJson(value));
            manifest.append("}}")
                    .append(++i < SerdeArea.cases().size() ? "," : "")
                    .append("\n");

            Files.writeString(
                    serdeDir.resolve("cases").resolve(id + ".json"),
                    SerdeArea.convert(type, value) + "\n",
                    StandardCharsets.UTF_8);
        }
        manifest.append("  ]\n}\n");
        Files.writeString(serdeDir.resolve("manifest.json"), manifest.toString(), StandardCharsets.UTF_8);
        System.out.println("Minted serde: surface.json (" + n + " arms), manifest.json + "
                + SerdeArea.cases().size() + " case fixture(s)");
    }

    private static String lowerCamel(String enumName) {
        String[] parts = enumName.toLowerCase().split("_");
        StringBuilder out = new StringBuilder(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            out.append(Character.toUpperCase(parts[i].charAt(0))).append(parts[i].substring(1));
        }
        return out.toString();
    }
}

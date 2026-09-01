package io.littlehorse.sdk.conformance;

import java.nio.file.Path;

/**
 * CLI dispatch for sdk-java's testee (contract: sdk-conformance/README.md).
 * compile/convert answer from CLI input alone — the exam path; mint is the
 * canon path, only ever run inside a PR where the diff is the review.
 */
public class ConformanceTestee {

    public static void main(String[] args) throws Exception {
        if (args.length == 1 && args[0].equals("list")) {
            WfsdkArea.caseIds().forEach(System.out::println);
            SerdeArea.caseIds().forEach(System.out::println);
            RegistrationsArea.caseIds().forEach(System.out::println);
            return;
        }
        if (args.length == 5 && args[0].equals("compile") && args[1].equals("--case") && args[3].equals("--variant")) {
            try {
                System.out.println(WfsdkArea.compile(args[2], args[4]));
            } catch (IllegalArgumentException e) {
                System.err.println(e.getMessage());
                System.exit(2);
            }
            return;
        }
        if (args.length >= 3 && args[0].equals("convert") && args[1].equals("--type")) {
            String value = null;
            if (args.length == 5 && args[3].equals("--value")) value = args[4];
            else if (args.length != 3) {
                System.err.println("usage: testee convert --type T [--value V]");
                System.exit(2);
            }
            try {
                System.out.println(SerdeArea.convert(args[2], value));
            } catch (IllegalArgumentException e) {
                System.err.println(e.getMessage());
                System.exit(2);
            }
            return;
        }
        if (args.length == 1 && args[0].equals("compile-all")) {
            StringBuilder out = new StringBuilder("{");
            boolean first = true;
            for (String id : WfsdkArea.caseIds()) {
                for (String variant : WfsdkArea.singles().containsKey(id)
                        ? new String[] {"feature"}
                        : new String[] {"base", "feature"}) {
                    if (!first) out.append(",");
                    first = false;
                    out.append("\"")
                            .append(id)
                            .append("/")
                            .append(variant)
                            .append("\":")
                            .append(WfsdkArea.compile(id, variant));
                }
            }
            System.out.println(out.append("}"));
            return;
        }
        if (args.length == 1 && args[0].equals("convert-batch")) {
            com.google.gson.Gson gson = new com.google.gson.Gson();
            StringBuilder out = new StringBuilder("{");
            boolean first = true;
            try (java.util.Scanner in = new java.util.Scanner(System.in)) {
                while (in.hasNextLine()) {
                    String line = in.nextLine().trim();
                    if (line.isEmpty()) continue;
                    java.util.Map<?, ?> req = gson.fromJson(line, java.util.Map.class);
                    if (!first) out.append(",");
                    first = false;
                    out.append("\"")
                            .append(req.get("id"))
                            .append("\":")
                            .append(SerdeArea.convert((String) req.get("type"), (String) req.get("value")));
                }
            }
            System.out.println(out.append("}"));
            return;
        }
        if (args.length == 5 && args[0].equals("registrations") && args[1].equals("--case") && args[3].equals("--variant")) {
            try {
                System.out.println(RegistrationsArea.answer(args[2], args[4]));
            } catch (IllegalArgumentException e) {
                System.err.println(e.getMessage());
                System.exit(2);
            }
            return;
        }
        if (args.length == 1 && args[0].equals("registrations-all")) {
            StringBuilder out = new StringBuilder("{");
            boolean first = true;
            for (String id : RegistrationsArea.caseIds()) {
                for (String variant : new String[] {"base", "feature"}) {
                    if (!first) out.append(",");
                    first = false;
                    out.append("\"").append(id).append("/").append(variant).append("\":")
                            .append(RegistrationsArea.answer(id, variant));
                }
            }
            out.append("}");
            System.out.println(out);
            return;
        }
        if (args.length == 5 && args[0].equals("fuzz") && args[1].equals("--seed") && args[3].equals("--ops")) {
            System.out.println(Fuzz.compile(Integer.parseInt(args[2]), Integer.parseInt(args[4])));
            return;
        }
        if (args.length == 2 && args[0].equals("mint")) {
            WfsdkAreaMint.mint(Path.of(args[1]).resolve("areas").resolve("wfsdk"));
            SerdeAreaMint.mint(Path.of(args[1]).resolve("areas").resolve("serde"));
            RegistrationsAreaMint.mint(Path.of(args[1]).resolve("areas").resolve("registrations"));
            return;
        }
        System.err.println(
                "usage: testee list | compile --case ID --variant base|feature | convert --type T [--value V] | mint CONFORMANCE_DIR");
        System.exit(2);
    }
}

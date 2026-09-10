package io.littlehorse.sdk.conformance;

import io.littlehorse.sdk.common.proto.Comparator;
import io.littlehorse.sdk.common.proto.VariableMutationType;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import java.util.ArrayList;
import java.util.List;

/**
 * Random dual-compile generator — implements the normative contract in
 * sdk-conformance/FUZZ.md exactly (PRNG, draw order, op table). No canon: the
 * runner cross-compares SDK outputs for the same seed.
 */
public final class Fuzz {

    private int state;

    private Fuzz(int seed) {
        this.state = seed;
    }

    private int nextInt(int bound) {
        state = state + 0x6D2B79F5;
        int t = state ^ (state >>> 15);
        t = t * (1 | state);
        t = (t + ((t ^ (t >>> 7)) * (61 | t))) ^ t;
        long draw = Integer.toUnsignedLong(t ^ (t >>> 14));
        return (int) (draw % bound);
    }

    static String compile(int seed, int ops) {
        Fuzz rnd = new Fuzz(seed);
        Workflow wf = Workflow.newWorkflow("fuzz-" + seed, thread -> {
            List<WfRunVariable> intVars = new ArrayList<>();
            for (int i = 0; i < ops; i++) {
                int k = rnd.nextInt(8);
                switch (k) {
                    case 0 -> intVars.add(thread.declareInt("v" + i));
                    case 1 -> thread.declareStr("v" + i);
                    case 2 -> thread.declareBool("v" + i);
                    case 3 -> thread.execute("task-" + rnd.nextInt(5));
                    case 4 -> thread.sleepSeconds(1 + rnd.nextInt(60));
                    case 5 -> thread.waitForEvent("evt-" + rnd.nextInt(5));
                    case 6 -> {
                        if (intVars.isEmpty()) {
                            intVars.add(thread.declareInt("v" + i));
                        } else {
                            thread.mutate(
                                    intVars.get(rnd.nextInt(intVars.size())),
                                    VariableMutationType.ADD,
                                    rnd.nextInt(10));
                        }
                    }
                    case 7 -> {
                        if (intVars.isEmpty()) {
                            thread.execute("task-" + rnd.nextInt(5));
                        } else {
                            WfRunVariable v = intVars.get(rnd.nextInt(intVars.size()));
                            int rhs = rnd.nextInt(10);
                            int branch = rnd.nextInt(5);
                            thread.doIf(
                                    thread.condition(v, Comparator.GREATER_THAN, rhs),
                                    body -> body.execute("branch-" + branch));
                        }
                    }
                    default -> throw new IllegalStateException();
                }
            }
        });
        return wf.compileWfToJson();
    }
}

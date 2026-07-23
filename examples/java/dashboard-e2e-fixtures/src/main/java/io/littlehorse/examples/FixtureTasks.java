package io.littlehorse.examples;

import io.littlehorse.sdk.worker.LHTaskMethod;

/**
 * The single task used by all fixture WfSpecs. Kept trivial and deterministic so the
 * Dashboard E2E assertions never depend on task-side timing or randomness.
 */
public class FixtureTasks {

    @LHTaskMethod(value = "dashe2e-greet", description = "Returns a fixed greeting for the given name.")
    public String greet(String name) {
        return "hello there, " + name;
    }
}

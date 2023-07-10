package io.littlehorse.jtests.test;

import io.littlehorse.sdk.client.LHClient;
import io.littlehorse.sdk.common.config.LHWorkerConfig;

/*
 * A `Test` is like a Unit Test but it is actually useful: it checks end-to-end
 * behavior of the LittleHorse System.
 *
 * A single `Test` class may be used to check multiple things, with the following
 * caveats:
 * 1. All of the sub-tests should be logically related, for example, testing
 *    conditionals by running the same WfSpec with various different inputs.
 * 2. All of the sub-tests should make use of the same LH Infrastructure, for example
 *    using the same WfSpec and TaskDef.
 *
 * Tests might be really quick (eg. a few seconds), if all it does is deploy a WfSpec
 * and run it with one input.
 *
 * Other tests might take longer, for example if we need to run Workflows with a
 * SLEEP node, and verify the behavior of SLEEP, or if we want to test time filters
 * on search.
 *
 * Multiple Test objects should be able to safely run in parallel and not stomp
 * over each other.
 *
 * Lastly, the `cleanup()` method should be idempotent and should remove all
 * test-specific resources (WfSpec, TaskDef, WfRun, etc) even if the `test()`
 * method threw a `TestFailure`.
 */
public abstract class Test {

    protected LHClient client;
    protected LHWorkerConfig workerConfig;

    public Test(LHClient client, LHWorkerConfig workerConfig) {
        this.client = client;
        this.workerConfig = workerConfig;
    }

    public abstract void cleanup() throws Exception;

    public abstract String getDescription();

    public abstract void test() throws Exception;
}

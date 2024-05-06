package io.littlehorse.examples;

import io.littlehorse.sdk.worker.LHTaskMethod;

public class MyWorker {

    @LHTaskMethod("task-1")
    public String task1() {
        return "task-1";
    }

    @LHTaskMethod("task-2")
    public String task2() {
        return "task-2";
    }

    @LHTaskMethod("task-3")
    public String task3() {
        return "task-3";
    }

    @LHTaskMethod("task-4")
    public String task4() {
        return "task-4";
    }
}

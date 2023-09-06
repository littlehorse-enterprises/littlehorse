package io.littlehorse.examples;

import io.littlehorse.sdk.worker.LHTaskMethod;

public class MyWorker {

    @LHTaskMethod("task-1")
    public String task1() {
        return "task-1";
    }

    @LHTaskMethod("task-2")
    public String task2() {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            out.append("asdfasdfasdfasdfjwth0238ahs9213t8hd");
        }
        return out.toString();
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

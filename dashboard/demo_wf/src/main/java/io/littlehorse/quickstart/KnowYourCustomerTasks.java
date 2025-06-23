package io.littlehorse.quickstart;

import io.littlehorse.sdk.worker.LHTaskMethod;

public class KnowYourCustomerTasks {
    @LHTaskMethod("demo-dashboard-task")
    public String verifyIdentity(String text) {
        return "This is the text: " + text;
    }
}

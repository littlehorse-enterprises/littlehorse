package io.littlehorse.sdk.worker;

public class CheckpointContext {
    private String logOutput;

    public CheckpointContext() {
        this.logOutput = "";
    }

    public void log(Object thing) {
        if (thing != null) {
            logOutput += thing.toString();
        } else {
            logOutput += "null";
        }
    }

    public String getLogOutput() {
        return logOutput;
    }
}

package io.littlehorse.jlib.wfsdk.internal;

import io.littlehorse.jlib.wfsdk.NodeOutput;

public class NodeOutputImpl implements NodeOutput {

    public String nodeName;
    public ThreadBuilderImpl parent;
    public String jsonPath;

    public NodeOutputImpl(String nodeName, ThreadBuilderImpl parent) {
        this.nodeName = nodeName;
        this.parent = parent;
    }

    public NodeOutputImpl jsonPath(String path) {
        if (jsonPath != null) {
            throw new RuntimeException("Cannot use jsonpath() twice on same node!");
        }
        NodeOutputImpl out = new NodeOutputImpl(nodeName, parent);
        out.jsonPath = path;
        return out;
    }

    public NodeOutputImpl timeout(int timeoutSeconds) {
        parent.addTimeoutToExtEvt(this, timeoutSeconds);
        return this;
    }
}

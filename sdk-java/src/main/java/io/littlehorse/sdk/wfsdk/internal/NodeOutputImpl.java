package io.littlehorse.sdk.wfsdk.internal;

import io.littlehorse.sdk.common.exception.LHMisconfigurationException;
import io.littlehorse.sdk.common.proto.LHPath.Selector;
import io.littlehorse.sdk.wfsdk.NodeOutput;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

class NodeOutputImpl implements NodeOutput {

    public String nodeName;
    public WorkflowThreadImpl parent;

    @Getter
    @Setter
    private String jsonPath;

    @Getter
    private List<Selector> lhPath;

    public NodeOutputImpl(String nodeName, WorkflowThreadImpl parent) {
        this.nodeName = nodeName;
        this.parent = parent;
        this.lhPath = new ArrayList<>();
    }

    public NodeOutputImpl jsonPath(String path) {
        if (jsonPath != null) {
            throw new RuntimeException("Cannot use jsonpath() twice on same node!");
        }
        NodeOutputImpl out = new NodeOutputImpl(nodeName, parent);
        out.setJsonPath(path);
        return out;
    }

    public NodeOutputImpl get(String index) {
        if (jsonPath != null) {
            throw new LHMisconfigurationException("Cannot use jsonPath() and get() on same var!");
        }
        NodeOutputImpl out = new NodeOutputImpl(nodeName, parent);
        out.getLhPath().add(Selector.newBuilder().setKey(index).build());
        return out;
    }
}

package io.littlehorse.jlib.wfsdk.internal;

import io.littlehorse.jlib.common.proto.VariableTypePb;
import io.littlehorse.jlib.wfsdk.WfRunVariable;

public class WfRunVariableImpl implements WfRunVariable {

    public String name;
    public VariableTypePb type;
    public String jsonPath;

    private ThreadBuilderImpl thread;

    public WfRunVariableImpl(
        String name,
        VariableTypePb type,
        ThreadBuilderImpl thread
    ) {
        this.name = name;
        this.type = type;
        this.thread = thread;
    }

    public WfRunVariableImpl jsonPath(String path) {
        if (jsonPath != null) {
            throw new RuntimeException("Cannot use jsonpath() twice on same var!");
        }
        WfRunVariableImpl out = new WfRunVariableImpl(name, type, thread);
        out.jsonPath = path;
        return out;
    }
}

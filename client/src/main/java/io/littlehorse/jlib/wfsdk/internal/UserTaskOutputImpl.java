package io.littlehorse.jlib.wfsdk.internal;

import io.littlehorse.jlib.wfsdk.UserTaskOutput;

public class UserTaskOutputImpl extends NodeOutputImpl implements UserTaskOutput {

    public UserTaskOutputImpl(String nodeName, ThreadBuilderImpl parent) {
        super(nodeName, parent);
    }
}

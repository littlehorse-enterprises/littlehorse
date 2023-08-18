package io.littlehorse.sdk.wfsdk.internal;

import io.littlehorse.sdk.common.proto.VariableAssignment;
import io.littlehorse.sdk.wfsdk.LHFormatString;
import java.util.ArrayList;
import java.util.List;

public class LHFormatStringImpl implements LHFormatString {

    private String format;
    private List<VariableAssignment> formatArgs;

    public LHFormatStringImpl(ThreadBuilderImpl thread, String format, Object[] args) {
        this.format = format;
        formatArgs = new ArrayList<>();
        for (Object arg : args) {
            formatArgs.add(thread.assignVariable(arg));
        }
    }

    public String getFormat() {
        return format;
    }

    public List<VariableAssignment> getArgs() {
        return formatArgs;
    }
}

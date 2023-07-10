package io.littlehorse.jlib.wfsdk;

public interface UserTaskOutput extends NodeOutput {
    public UserTaskOutput withNotes(String notes);

    public UserTaskOutput withNotes(WfRunVariable notes);

    public UserTaskOutput withNotes(LHFormatString notes);
}

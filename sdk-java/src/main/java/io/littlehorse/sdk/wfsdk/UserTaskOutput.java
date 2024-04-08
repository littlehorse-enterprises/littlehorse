package io.littlehorse.sdk.wfsdk;

public interface UserTaskOutput extends NodeOutput {
    public UserTaskOutput withNotes(String notes);

    public UserTaskOutput withNotes(WfRunVariable notes);

    public UserTaskOutput withNotes(LHFormatString notes);

    UserTaskOutput withOnCancelException(String exceptionName);

    UserTaskOutput withOnCancelException(WfRunVariable exceptionName);
}

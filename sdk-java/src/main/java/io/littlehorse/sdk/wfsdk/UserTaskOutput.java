package io.littlehorse.sdk.wfsdk;

/**
 * Node output API for UserTask nodes.
 */
public interface UserTaskOutput extends NodeOutput {
    /**
     * Sets static notes shown on the UserTask.
     *
     * @param notes static notes string
     * @return this node output
     */
    public UserTaskOutput withNotes(String notes);

    /**
     * Sets dynamic notes from a WfRunVariable.
     *
     * @param notes is a WfRunVariable containing notes
     * @return this node output
     */
    public UserTaskOutput withNotes(WfRunVariable notes);

    /**
     * Sets formatted notes built at runtime.
     *
     * @param notes formatted notes expression
     * @return this node output
     */
    public UserTaskOutput withNotes(LHFormatString notes);

    /**
     * Sets the exception name to throw if the user task is cancelled.
     *
     * @param exceptionName exception name
     * @return this node output
     */
    UserTaskOutput withOnCancellationException(String exceptionName);

    /**
     * Sets cancellation exception name from a WfRunVariable.
     *
     * @param exceptionName is a WfRunVariable with exception name
     * @return this node output
     */
    UserTaskOutput withOnCancellationException(WfRunVariable exceptionName);
}

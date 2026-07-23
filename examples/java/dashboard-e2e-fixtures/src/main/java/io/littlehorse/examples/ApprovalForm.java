package io.littlehorse.examples;

import io.littlehorse.sdk.usertask.annotations.UserTaskField;

/**
 * Minimal user-task form for the `dashe2e-usertask` fixture. Its only purpose is to
 * give the Dashboard a USER_TASK node that parks in a RUNNING state to render.
 */
public class ApprovalForm {

    @UserTaskField(description = "Whether the request is approved.", displayName = "Approved")
    public boolean approved;
}

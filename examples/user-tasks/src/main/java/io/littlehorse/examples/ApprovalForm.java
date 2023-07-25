package io.littlehorse.examples;

import io.littlehorse.sdk.usertask.annotations.UserTaskField;

public class ApprovalForm {

    @UserTaskField(
        displayName = "Approved?",
        description = "Reply 'true' if this is an acceptable request."
    )
    public boolean isApproved;
}

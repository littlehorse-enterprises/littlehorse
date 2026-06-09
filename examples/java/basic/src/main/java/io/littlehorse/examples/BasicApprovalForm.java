package io.littlehorse.examples;

import io.littlehorse.sdk.usertask.annotations.UserTaskField;

public class BasicApprovalForm {

    @UserTaskField(displayName = "Approved?", description = "Reply 'true' if this request looks good.")
    public boolean isApproved;
}

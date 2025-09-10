package io.littlehorse.examples;

import io.littlehorse.sdk.usertask.annotations.UserTaskField;

public class ItemRequestForm {

    @UserTaskField(
        description = "The item you are requesting.",
        displayName = "Your Request"
    )
    public String requestedItem;

    @UserTaskField(
        description = "Why you need this request.",
        displayName = "Request Justification"
    )
    public String justification;
}

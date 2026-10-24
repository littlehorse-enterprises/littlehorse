package io.littlehorse.examples;

import io.littlehorse.sdk.worker.LHStructDef;
import io.littlehorse.sdk.worker.LHStructField;

@LHStructDef(value = "approval-form", description = "empty")
public class ApprovalForm {

    @LHStructField(description = "Whether the request is approved or not.")
    public boolean isApproved;

    public ApprovalForm() {}

    public ApprovalForm(boolean isApproved) {
        this.isApproved = isApproved;
    }

    public boolean isApproved() {
        return isApproved;
    }

    public void setApproved(boolean approved) {
        isApproved = approved;
    }
}

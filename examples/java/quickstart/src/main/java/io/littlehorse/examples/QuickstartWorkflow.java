package io.littlehorse.examples;

import io.littlehorse.sdk.common.proto.LHErrorType;
import io.littlehorse.sdk.wfsdk.NodeOutput;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.WorkflowThread;

public class QuickstartWorkflow {

    public static final String WORKFLOW_NAME = "quickstart";
    public static final String IDENTITY_VERIFIED_EVENT = "identity-verified";
    public static final String VERIFY_IDENTITY_TASK = "verify-identity";
    public static final String NOTIFY_CUSTOMER_VERIFIED_TASK = "notify-customer-verified";
    public static final String NOTIFY_CUSTOMER_NOT_VERIFIED_TASK = "notify-customer-not-verified";

    public void quickstartWf(WorkflowThread wf) {
        WfRunVariable fullName = wf.declareStr("full-name").searchable().required();
        WfRunVariable email = wf.declareStr("email").searchable().required();
        WfRunVariable ssn = wf.declareInt("ssn").masked().required();
        WfRunVariable identityVerified = wf.declareBool("identity-verified").searchable();

        wf.execute(VERIFY_IDENTITY_TASK, fullName, email, ssn).withRetries(3);

        NodeOutput identityVerificationResult = wf.waitForEvent(IDENTITY_VERIFIED_EVENT)
                .timeout(60 * 5)
                .withCorrelationId(email)
                .registeredAs(Boolean.class);

        wf.handleError(identityVerificationResult, LHErrorType.TIMEOUT, handler -> {
            handler.execute(NOTIFY_CUSTOMER_NOT_VERIFIED_TASK, fullName, email);
            handler.fail("customer-not-verified", "Unable to verify customer identity in time.");
        });

        identityVerified.assign(identityVerificationResult);

        wf.doIf(identityVerified.isEqualTo(true), ifBody -> {
                    ifBody.execute(NOTIFY_CUSTOMER_VERIFIED_TASK, fullName, email);
                })
                .doElse(elseBody -> {
                    elseBody.execute(NOTIFY_CUSTOMER_NOT_VERIFIED_TASK, fullName, email);
                });
    }

    public Workflow getWorkflow() {
        return Workflow.newWorkflow(WORKFLOW_NAME, this::quickstartWf);
    }
}

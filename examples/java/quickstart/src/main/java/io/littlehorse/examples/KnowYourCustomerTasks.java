package io.littlehorse.examples;

import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.LHType;
import java.util.Random;

public class KnowYourCustomerTasks {

    private final Random random = new Random();

    @LHTaskMethod(QuickstartWorkflow.VERIFY_IDENTITY_TASK)
    public String verifyIdentity(String fullName, String email, @LHType(masked = true) int ssn) {
        if (random.nextDouble() < 0.25) {
            throw new RuntimeException("The external identity verification API is down");
        }

        return "Successfully called external API to request verification for " + fullName + " at " + email;
    }

    @LHTaskMethod(QuickstartWorkflow.NOTIFY_CUSTOMER_VERIFIED_TASK)
    public String notifyCustomerVerified(String fullName, String email) {
        return "Notification sent to customer " + fullName + " at " + email + " that their identity has been verified";
    }

    @LHTaskMethod(QuickstartWorkflow.NOTIFY_CUSTOMER_NOT_VERIFIED_TASK)
    public String notifyCustomerNotVerified(String fullName, String email) {
        return "Notification sent to customer " + fullName + " at " + email
                + " that their identity has not been verified";
    }
}

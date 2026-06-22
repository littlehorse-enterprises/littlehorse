using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Workflow.Spec;

namespace QuickstartExample;

public static class QuickstartWorkflow
{
    public const string WorkflowName = "quickstart";
    public const string IdentityVerifiedEvent = "identity-verified";
    public const string VerifyIdentityTask = "verify-identity";
    public const string NotifyCustomerVerifiedTask = "notify-customer-verified";
    public const string NotifyCustomerNotVerifiedTask = "notify-customer-not-verified";

    public static Workflow GetWorkflow()
    {
        void EntryPoint(WorkflowThread wf)
        {
            var fullName = wf.DeclareStr("full-name").Searchable().Required();
            var email = wf.DeclareStr("email").Searchable().Required();
            var ssn = wf.DeclareInt("ssn").Masked().Required();
            var identityVerified = wf.DeclareBool("identity-verified").Searchable();

            wf.Execute(VerifyIdentityTask, fullName, email, ssn).WithRetries(3);

            var identityVerificationResult = wf.WaitForEvent(IdentityVerifiedEvent)
                .WithTimeout(60 * 5)
                .WithCorrelationId(email);

            wf.HandleError(
                identityVerificationResult,
                LHErrorType.Timeout,
                handler =>
                {
                    handler.Execute(NotifyCustomerNotVerifiedTask, fullName, email);
                    handler.Fail("customer-not-verified", "Unable to verify customer identity in time.");
                }
            );

            identityVerified.Assign(identityVerificationResult);

            wf.DoIf(
                wf.Condition(identityVerified, Comparator.Equals, true),
                ifThread => ifThread.Execute(NotifyCustomerVerifiedTask, fullName, email),
                elseThread => elseThread.Execute(NotifyCustomerNotVerifiedTask, fullName, email)
            );
        }

        return new Workflow(WorkflowName, EntryPoint);
    }
}

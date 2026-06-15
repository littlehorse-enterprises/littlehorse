using LittleHorse.Sdk.Worker;

namespace QuickstartExample;

public class KnowYourCustomerTasks
{
    private static readonly Random Random = new();

    [LHTaskMethod(QuickstartWorkflow.VerifyIdentityTask)]
    public Task<string> VerifyIdentity(string fullName, string email, int ssn)
    {
        _ = ssn;
        if (Random.NextDouble() < 0.25)
        {
            throw new Exception("The external identity verification API is down");
        }

        return Task.FromResult(
            "Successfully called external API to request verification for " + fullName + " at " + email
        );
    }

    [LHTaskMethod(QuickstartWorkflow.NotifyCustomerNotVerifiedTask)]
    public Task<string> NotifyCustomerNotVerified(string fullName, string email)
    {
        return Task.FromResult(
            "Notification sent to customer " + fullName + " at " + email
            + " that their identity has not been verified"
        );
    }

    [LHTaskMethod(QuickstartWorkflow.NotifyCustomerVerifiedTask)]
    public Task<string> NotifyCustomerVerified(string fullName, string email)
    {
        return Task.FromResult(
            "Notification sent to customer " + fullName + " at " + email
            + " that their identity has been verified"
        );
    }
}

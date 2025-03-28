using LittleHorse.Sdk.UserTask;

namespace UserTasksExample;

public class ApprovalForm
{
    [UserTaskField(
        DisplayName = "Approved?",
        Description = "Reply 'false' if this is an acceptable request.")]
    public bool IsApproved;
}
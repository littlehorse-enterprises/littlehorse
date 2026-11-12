using LittleHorse.Sdk.Worker;

namespace UserTasksExample;

[LHStructDef("approval-form")]
public class ApprovalForm
{
    [LHStructField(name: "isApproved", description: "Whether the request is approved.")]
    public bool IsApproved { get; set; }
}

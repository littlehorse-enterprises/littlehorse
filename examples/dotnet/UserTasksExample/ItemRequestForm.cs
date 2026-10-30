using LittleHorse.Sdk.Worker;

namespace UserTasksExample;

[LHStructDef("item-request-form")]
public class ItemRequestForm
{
    [LHStructField(name: "requestedItem", description: "The item you are requesting.")]
    public string RequestedItem { get; set; } = string.Empty;

    [LHStructField(name: "justification", description: "Why you need this request.")]
    public string Justification { get; set; } = string.Empty;
}

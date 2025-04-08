using LittleHorse.Sdk.UserTask;

namespace UserTasksExample;

public class ItemRequestForm
{
    [UserTaskField(
        DisplayName = "Your Request", 
        Description = "The item you are requesting.")]
    public string? RequestedItem;
    
    [UserTaskField(
        DisplayName = "Request Justification", 
        Description = "Why you need this request.")]
    public string? Justification;
}
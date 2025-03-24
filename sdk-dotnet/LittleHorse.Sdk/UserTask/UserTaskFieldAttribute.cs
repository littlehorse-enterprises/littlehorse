namespace LittleHorse.Sdk.UserTask;

[AttributeUsage(AttributeTargets.Field)]
public class UserTaskFieldAttribute : Attribute
{
    public string Description = "";

    public string DisplayName = "";

    public bool Required = true;

    public UserTaskFieldAttribute()
    {
    }
}
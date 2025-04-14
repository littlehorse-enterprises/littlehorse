namespace LittleHorse.Sdk.UserTask;

/// <summary>
/// Apply this attribute to a field of the user form class.
/// </summary>
[AttributeUsage(AttributeTargets.Field)]
public class UserTaskFieldAttribute : Attribute
{
    /// <value>Property <c>Description</c> represents the description to be displayed.</value>
    public string Description = "";
    
    /// <value>Property <c>DisplayName</c> represents the name of the field to be displayed.</value>
    public string DisplayName = "";
    
    /// <value>Property <c>Required</c> marks if a field is required.</value>
    public bool Required = true;

    /// <summary>
    /// The constructor to add properties in the attribute UserTaskField
    /// </summary>
    public UserTaskFieldAttribute()
    {
    }
}
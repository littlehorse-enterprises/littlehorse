using System.Reflection;
using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Helper;

namespace LittleHorse.Sdk.UserTask;

/// <summary>
/// Represents the schema for a user task.
/// </summary>
public class UserTaskSchema
{
    private PutUserTaskDefRequest? _compiled;
    private readonly object _taskObject;
    private readonly string _userTaskDefName;
    
    /// <summary>
    /// Constructs a UserTaskSchema with the specified task object and user task definition name.
    /// </summary>
    /// <param name="taskObject">The task object</param>
    /// <param name="userTaskDefName">The name of the user task definition</param>
    public UserTaskSchema(object taskObject, string userTaskDefName) 
    {
        _taskObject = taskObject;
        _userTaskDefName = userTaskDefName;
    }
    
    /// <summary>
    /// Compiles the user task schema into a PutUserTaskDefRequest.
    /// - Fields in task object should be primitive types.
    /// - If field has not set a <c>DisplayName</c>, it will assign the field name.
    /// - Fields in user task form are <c>required</c> by default.
    /// </summary>
    /// <returns>The compiled PutUserTaskDefRequest</returns>
    public PutUserTaskDefRequest Compile()
    {
        return _compiled ??= CompileHelper();
    }
    
    private PutUserTaskDefRequest CompileHelper() 
    {
        var putUserTaskDefRequest = new PutUserTaskDefRequest();
        Type taskObjectType = _taskObject.GetType();
        foreach (FieldInfo field in taskObjectType.GetFields())
        {
            UserTaskFieldAttribute userTaskFieldAttribute = 
                (UserTaskFieldAttribute) field.GetCustomAttribute(typeof(UserTaskFieldAttribute))!;
            if (userTaskFieldAttribute == null) 
                continue;
            VariableType type = LHMappingHelper.DotNetTypeToLHVariableType(field.FieldType);
            
            if (type == VariableType.JsonArr || type == VariableType.JsonObj || type == VariableType.Bytes)
            {
                throw new ArgumentException($"Only primitive types supported for UserTaskField. Field {field.Name}" +
                                            $" is of type {type}");
            }

            var userTaskField = new UserTaskField { Name = field.Name, Type = type };
            
            if (!string.IsNullOrEmpty(userTaskFieldAttribute.Description)) 
            {
                userTaskField.Description = userTaskFieldAttribute.Description;
            }

            userTaskField.DisplayName = !string.IsNullOrEmpty(userTaskFieldAttribute.DisplayName) ? 
                userTaskFieldAttribute.DisplayName : field.Name;
            userTaskField.Required = userTaskFieldAttribute.Required;

            putUserTaskDefRequest.Fields.Add(userTaskField);
        }
        
        putUserTaskDefRequest.Name = _userTaskDefName;
        return putUserTaskDefRequest;
    }
}
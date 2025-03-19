using System.Reflection;
using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Helper;

namespace LittleHorse.Sdk.UserTask;

public class UserTaskSchema
{
    private PutUserTaskDefRequest? _compiled;
    private readonly object _taskObject;
    private readonly string _userTaskDefName;
    
    public UserTaskSchema(object taskObject, string userTaskDefName) 
    {
        _taskObject = taskObject;
        _userTaskDefName = userTaskDefName;
        _compiled = null;
    }

    public PutUserTaskDefRequest Compile() 
    {
        if (_compiled == null) CompileHelper();

        return _compiled;
    }
    
    private void CompileHelper() 
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
            
            if (!string.IsNullOrEmpty(userTaskFieldAttribute.DisplayName)) 
            {
                userTaskField.DisplayName = userTaskFieldAttribute.DisplayName;
            }
            
            userTaskField.DisplayName = !string.IsNullOrEmpty(userTaskFieldAttribute.DisplayName) ? userTaskFieldAttribute.DisplayName : field.Name;
            
            putUserTaskDefRequest.Fields.Add(userTaskField);
        }
        
        putUserTaskDefRequest.Name = _userTaskDefName;
        _compiled = putUserTaskDefRequest;
    }
}
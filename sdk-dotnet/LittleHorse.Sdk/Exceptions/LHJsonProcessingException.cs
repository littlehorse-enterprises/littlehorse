namespace LittleHorse.Sdk.Exceptions;

///<summary>
/// Maps Exception that contains jsons serialization or deserialization errors. 
/// </summary>
public class LHJsonProcessingException: Exception
{
    ///<summary>
    /// Constructor of the Exception which represents a problem of jsons serialization or deserialization. 
    /// </summary>
    /// <param name="message"> A custom message.</param>
    public LHJsonProcessingException(string message): base(message) { }
}
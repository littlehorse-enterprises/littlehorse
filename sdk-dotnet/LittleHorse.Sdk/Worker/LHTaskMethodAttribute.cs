namespace LittleHorse.Sdk.Worker
{
    /// <summary>
    /// Attribute used to indicate that the annotated method should be used as the method to execute a
    /// Task in the LH Dotnet Task Worker library.
    /// </summary>
    [AttributeUsage(AttributeTargets.Method)]
    public class LHTaskMethodAttribute : Attribute
    {
        /// <summary>
        /// This is the value of the attribute; it corresponds to the name of the TaskDef executed by
        /// the annotated Method.
        /// </summary>
        public string Value;

        /// <summary>
        /// Constructor of the attribute.
        /// </summary>
        /// <param name="value">This is the name of the TaskDef.</param>
        public LHTaskMethodAttribute(string value)
        {
            Value = value;
        }
    }
}

namespace LittleHorse.Worker.Attributes
{
    /// <summary>
    /// Attribute used to indicate that the annotated method should be used as the method to execute a
    /// Task in the LH Java Task Worker library.
    /// </summary>
    [AttributeUsage(AttributeTargets.Method, AllowMultiple = false)]
    public class LHTaskMethodAttribute : Attribute
    {
        /// <summary>
        /// This is the value of the attribute; it corresponds to the name of the TaskDef executed by
        /// the annotated Method.
        /// </summary>
        public string Value;

        public LHTaskMethodAttribute(string value)
        {
            Value = value;
        }
    }
}

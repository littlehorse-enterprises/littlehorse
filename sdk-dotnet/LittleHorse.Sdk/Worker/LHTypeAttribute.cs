namespace LittleHorse.Sdk.Worker
{
    /// <summary>
    /// Attribute for adding metadata based on the target type.
    /// <p>
    /// This annotation can be applied to either a method or a method parameter:
    /// </p>
    /// - Method: When applied to a method, the metadata will be added to a {@code NodeOutput}.
    /// - Method Parameter: When applied to a method parameter, the metadata will be added to a {@code VariableDef}.
    /// </summary>
    ///
    [AttributeUsage(AttributeTargets.Method | AttributeTargets.Parameter)]
    public class LHTypeAttribute : Attribute
    {
        /// <summary>
        /// Indicates whether the value should be masked.
        /// </summary>
        public bool Masked { get; }

        /// <summary>
        /// An optional name associated with the attribute.
        /// </summary>
        public string Name { get; }
        
        public LHTypeAttribute(bool masked)
        {
            Masked = masked;
            Name = string.Empty;
        }
        
        public LHTypeAttribute(bool masked, string name)
        {
            Masked = masked;
            Name = name;
        }
    }
}

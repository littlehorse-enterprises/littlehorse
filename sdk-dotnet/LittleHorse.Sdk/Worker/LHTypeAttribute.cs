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
        
        /// <summary>
        /// Constructor of the attribute.
        /// </summary>
        /// <param name="masked">A bool input that makes a method response or an input param be masked.</param>
        public LHTypeAttribute(bool masked)
        {
            Masked = masked;
            Name = string.Empty;
        }
        
        /// <summary>
        /// Constructor of the attribute.
        /// </summary>
        /// <param name="masked">A bool input that makes a method response or an input param be masked.</param>
        /// <param name="name">The name of the masked field.</param>
        public LHTypeAttribute(bool masked, string name)
        {
            Masked = masked;
            Name = name;
        }
    }
}

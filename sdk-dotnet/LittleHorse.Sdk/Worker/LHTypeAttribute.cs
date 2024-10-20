namespace LittleHorse.Sdk.Worker
{
    /// <summary>
    /// Attribute used to indicate that the annotated method should be used as the method to execute a
    /// Task in the LH Dotnet Task Worker library.
    /// </summary>
    [AttributeUsage(AttributeTargets.Method | AttributeTargets.Parameter)]
    public class LHTypeAttribute : Attribute
    {
        /// <summary>
        /// This is the masked flag attribute to secure sensible params in the annotated method
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

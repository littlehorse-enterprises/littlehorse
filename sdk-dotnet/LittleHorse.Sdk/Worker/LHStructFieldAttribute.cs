using System;

namespace LittleHorse.Sdk.Worker
{
    /// <summary>
    /// Specifies LittleHorse struct field metadata such as name overrides and masking.
    /// </summary>
    [AttributeUsage(AttributeTargets.Property | AttributeTargets.Method)]
    public sealed class LHStructFieldAttribute : Attribute
    {
        /// <summary>
        /// Optional field name override.
        /// </summary>
        public string Name { get; }

        /// <summary>
        /// Indicates whether the field should be masked in type definitions.
        /// </summary>
        public bool Masked { get; }

        /// <summary>
        /// Creates a struct field attribute.
        /// </summary>
        /// <param name="name">Optional field name override.</param>
        /// <param name="masked">Whether the field should be masked.</param>
        public LHStructFieldAttribute(string name = "", bool masked = false)
        {
            Name = name;
            Masked = masked;
        }
    }
}

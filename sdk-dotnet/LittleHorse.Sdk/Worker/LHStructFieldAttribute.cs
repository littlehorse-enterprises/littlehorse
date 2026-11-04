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
        /// Optional human-readable description of this field's purpose.
        /// </summary>
        public string Description { get; }

        /// <summary>
        /// Creates a struct field attribute.
        /// </summary>
        /// <param name="name">Optional field name override.</param>
        /// <param name="masked">Whether the field should be masked.</param>
        /// <param name="description">Optional description of the field's purpose.</param>
        public LHStructFieldAttribute(string name = "", bool masked = false, string description = "")
        {
            Name = name;
            Masked = masked;
            Description = description;
        }
    }
}

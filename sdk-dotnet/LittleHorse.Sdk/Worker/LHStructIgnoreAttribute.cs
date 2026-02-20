using System;

namespace LittleHorse.Sdk.Worker
{
	/// <summary>
	/// Marks a property or accessor to be ignored in struct definitions.
	/// </summary>
	[AttributeUsage(AttributeTargets.Property | AttributeTargets.Method)]
	public sealed class LHStructIgnoreAttribute : Attribute
	{
	}
}

using System.Reflection;
using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Helper;
using Microsoft.Extensions.Logging;

namespace LittleHorse.Sdk.Worker
{
    internal sealed class LHTaskParameter
    {
        private readonly VariableDef _variableDef;

        internal VariableDef VariableDef => _variableDef;

        internal LHTaskParameter(ParameterInfo parameter, int index, ILogger? logger)
        {
            LHTypeMetadata metadata = LHTypeMetadata.From(parameter);
            metadata.ValidateLHArrayUsage(parameter.ParameterType, LHTypeMetadata.ValidationContext.Parameter, parameter.Name ?? $"param{index}");

            string? paramName = parameter.Name;
            if (string.IsNullOrWhiteSpace(paramName))
            {
                logger?.LogWarning("Unable to inspect parameter names using reflection; using parameter index as name.");
                paramName = $"param{index}";
            }

            if (!string.IsNullOrWhiteSpace(metadata.Name))
            {
                paramName = metadata.Name;
            }

            LHClassType lhClassType = LHTypeMetadata.ResolveTaskDefinitionType(parameter.ParameterType, metadata.IsLHArray);
            TypeDefinition typeDef = lhClassType.GetTypeDefinition();
            typeDef.Masked = metadata.Masked;

            _variableDef = new VariableDef
            {
                Name = paramName,
                TypeDef = typeDef
            };
        }
    }
}
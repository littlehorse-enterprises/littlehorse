using System.Runtime.CompilerServices;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Logging.Abstractions;

[assembly: InternalsVisibleTo("LittleHorse.Sdk.Tests")]

namespace LittleHorse.Sdk.Internal
{
    internal static class LHLoggerFactoryProvider
    {
        private static ILoggerFactory? _loggerFactory;

        internal static void Initialize(ILoggerFactory? loggerFactory = null)
        {
            _loggerFactory = loggerFactory ?? NullLoggerFactory.Instance;
        }

        internal static ILogger<T> GetLogger<T>()
        {
            if (_loggerFactory != null)
            {
                return _loggerFactory.CreateLogger<T>();
            }

            throw new InvalidOperationException("_loggerFactory does not have a valid value and it is trying to create a logger instance.");
        }
    }
}

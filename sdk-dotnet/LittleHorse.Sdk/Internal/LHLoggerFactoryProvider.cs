using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Logging.Abstractions;

namespace LittleHorse.Sdk.Internal
{
    public static class LHLoggerFactoryProvider
    {
        private static ILoggerFactory? _loggerFactory;

        public static void Initialize(ILoggerFactory? loggerFactory = null)
        {
            _loggerFactory = loggerFactory ?? NullLoggerFactory.Instance;
        }

        public static ILogger<T> GetLogger<T>()
        {
            if (_loggerFactory != null)
            {
                return _loggerFactory.CreateLogger<T>();
            }

            throw new InvalidOperationException("_loggerFactory does not have a valid value and it is trying to create a logger instance.");
        }
    }
}

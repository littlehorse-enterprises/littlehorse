using LittleHorseSDK.Common.proto;

namespace LittleHorse.Common.Exceptions
{
    [Serializable]
    public class LHApiException : Exception
    {
        public LHApiException() : base(CreateBaseMessage()) { }

        public LHApiException(string message) : base (CreateBaseMessage(message)) { }

        public LHApiException(string message, Exception innerException) : base(CreateBaseMessage(message), innerException) {}

        private static string CreateBaseMessage()
        {
            return $"Failed contacting LH API.";
        }

        private static string CreateBaseMessage(string message)
        {
            return $"Failed contacting LH API: {message}";
        }

    }
}

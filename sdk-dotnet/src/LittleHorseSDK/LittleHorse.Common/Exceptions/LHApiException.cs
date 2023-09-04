using LittleHorseSDK.Common.proto;

namespace LittleHorse.Common.Exceptions
{
    [Serializable]
    public class LHApiException : Exception
    {
        public LHResponseCode Code { get; init; }

        public LHApiException() : base(CreateBaseMessage()) { }

        public LHApiException(string message) : base (CreateBaseMessage(message)) { }

        public LHApiException(string message, Exception innerException) : base(CreateBaseMessage(message), innerException) {}

        public LHApiException(string message, LHResponseCode code) : base(CreateBaseMessage(message, code))
        {
            Code = code;
        }

        public LHApiException(string message, LHResponseCode code, Exception innerException) : base(CreateBaseMessage(message, code), innerException)
        {
            Code = code;
        }

        private static string CreateBaseMessage()
        {
            return $"Failed contacting LH API.";
        }

        private static string CreateBaseMessage(string message)
        {
            return $"Failed contacting LH API: {message}";
        }

        private static string CreateBaseMessage(string message, LHResponseCode code)
        {
            return $"Failed contacting LH API: {code} : {message}";
        }
    }
}

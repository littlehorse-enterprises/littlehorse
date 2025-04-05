using System.Net;

namespace LittleHorse.Sdk.Exceptions
{
    internal class LHAuthorizationServerException : Exception
    {
        internal HttpStatusCode? Code { get; init; }
        internal LHAuthorizationServerException() : base() { }

        internal LHAuthorizationServerException(string message) : base(message) { }

        internal LHAuthorizationServerException(string message, Exception innerException) : base(message, innerException) { }

        internal LHAuthorizationServerException(HttpStatusCode code) : base()
        {
            Code = code;
        }

        internal LHAuthorizationServerException(HttpStatusCode code, string message) : base(message)
        {
            Code = code;
        }

        internal LHAuthorizationServerException(HttpStatusCode code, string message, Exception innerException) : base(message, innerException)
        {
            Code = code;
        }
    }
}

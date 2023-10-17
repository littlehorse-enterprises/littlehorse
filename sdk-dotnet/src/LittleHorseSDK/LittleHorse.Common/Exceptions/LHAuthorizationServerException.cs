using System.Net;

namespace LittleHorse.Common.Exceptions
{
    public class LHAuthorizationServerException : Exception
    {
        public HttpStatusCode? Code { get; init; }
        public LHAuthorizationServerException() : base() { }

        public LHAuthorizationServerException(string message) : base(message) { }

        public LHAuthorizationServerException(string message, Exception innerException) : base(message, innerException) { }

        public LHAuthorizationServerException(HttpStatusCode code) : base()
        {
            Code = code;
        }

        public LHAuthorizationServerException(HttpStatusCode code, string message) : base(message)
        {
            Code = code;
        }

        public LHAuthorizationServerException(HttpStatusCode code, string message, Exception innerException) : base(message, innerException)
        {
            Code = code;
        }
    }
}

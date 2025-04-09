using System.Net;

namespace LittleHorse.Sdk.Exceptions
{
    /// <summary>
    /// Indicates an Exception problem from a client requesting authorization to an LH server.
    /// </summary>
    public class LHAuthorizationServerException : Exception
    {
        /// <value>
        /// The http status code associated with the authorization exception.
        /// </value>
        public HttpStatusCode? Code { get; init; }
        
        
        /// <summary>
        /// Empty Constructor of the Exception which represents a problem with the authorization to an LH server.
        /// </summary>
        public LHAuthorizationServerException() : base() { }

        /// <summary>
        /// Constructor of the Exception which represents a problem with the authorization to an LH server.
        /// </summary>
        /// <param name="message">The message that will be included in the exception.</param>
        public LHAuthorizationServerException(string message) : base(message) { }

        /// <summary>
        /// Constructor of the Exception which represents a problem with the authorization to an LH server.
        /// </summary>
        /// <param name="message"> A custom error message.</param>
        /// <param name="innerException">This is another exception that caused this one.</param>
        public LHAuthorizationServerException(string message, Exception innerException) : base(message, innerException) { }

        /// <summary>
        /// Constructor of the Exception which represents a problem with the authorization to an LH server.
        /// </summary>
        /// <param name="code">The http status code associated with the authorization exception.</param>
        public LHAuthorizationServerException(HttpStatusCode code) : base()
        {
            Code = code;
        }

        /// <summary>
        /// Constructor of the Exception which represents a problem with the authorization to an LH server.
        /// </summary>
        /// <param name="code">The http status code associated with the authorization exception.</param>
        /// <param name="message">A custom error message.</param>
        public LHAuthorizationServerException(HttpStatusCode code, string message) : base(message)
        {
            Code = code;
        }

        /// <summary>
        /// Constructor of the Exception which represents a problem with the authorization to an LH server.
        /// </summary>
        /// <param name="code">The http status code associated with the authorization exception.</param>
        /// <param name="message">A custom error message.</param>
        /// <param name="innerException">This is another exception that caused this one.</param>
        public LHAuthorizationServerException(HttpStatusCode code, string message, Exception innerException) : base(message, innerException)
        {
            Code = code;
        }
    }
}

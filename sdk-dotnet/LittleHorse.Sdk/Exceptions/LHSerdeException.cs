namespace LittleHorse.Sdk.Exceptions
{
    ///<summary>
    /// Maps Exception that contains serialization or deserialization of objects in LH context.
    /// </summary>
    public class LHSerdeException : Exception
    {
        ///<summary>
        /// Empty Constructor of the Exception which represents a problem with
        /// serialization or deserialization of objects in LH context.
        /// </summary>
        public LHSerdeException() : base() { }

        ///<summary>
        /// Constructor of the Exception which represents a problem with
        /// serialization or deserialization of objects in LH context.
        /// </summary>
        /// <param name="message"> A custom message.</param>
        public LHSerdeException(string message) : base(message) { }

        ///<summary>
        /// Constructor of the Exception which represents a problem with
        /// serialization or deserialization of objects in LH context.
        /// </summary>
        /// <param name="message"> A custom message.</param>
        /// <param name="innerException">This is another exception that caused this one.</param>
        public LHSerdeException(string message, Exception innerException) : base(message, innerException) { }

    }
}

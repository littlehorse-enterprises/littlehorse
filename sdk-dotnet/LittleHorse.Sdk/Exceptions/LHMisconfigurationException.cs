namespace LittleHorse.Sdk.Exceptions
{
    ///<summary>
    /// Maps an Exception that contains problems with LH clients configurations. 
    /// </summary>
    public class LHMisconfigurationException : Exception
    {
        ///<summary>
        /// Empty Constructor of the Exception which represents a problem with LH client configurations. 
        /// </summary>
        public LHMisconfigurationException() : base() { }

        ///<summary>
        /// Constructor of the Exception which represents a problem with LH client configurations. 
        /// </summary>
        /// <param name="message"> A custom message.</param>
        public LHMisconfigurationException(string message) : base(message) { }

        ///<summary>
        /// Constructor of the Exception which represents a problem with LH client configurations. 
        /// </summary>
        /// <param name="message"> A custom message.</param>
        /// <param name="innerException">This is another exception that caused this one.</param>
        public LHMisconfigurationException(string message, Exception innerException) : base(message, innerException) { }
    }
}

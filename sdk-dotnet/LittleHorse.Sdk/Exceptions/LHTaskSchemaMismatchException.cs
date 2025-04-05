namespace LittleHorse.Sdk.Exceptions
{
    ///<summary>
    /// Maps an Exception that contains differences between TaskDef registered and current LHTaskMethod signature.
    /// </summary>
    public class LHTaskSchemaMismatchException : Exception
    {
        ///<summary>
        /// Empty Constructor of the Exception which contains differences between TaskDef registered
        /// and current LHTaskMethod signature.
        /// </summary>
        public LHTaskSchemaMismatchException() { }

        ///<summary>
        /// Constructor of the Exception which contains differences between TaskDef registered
        /// and current LHTaskMethod signature.
        /// </summary>
        /// <param name="message"> A custom message.</param>
        public LHTaskSchemaMismatchException(string message) : base(message) { }

        ///<summary>
        /// Constructor of the Exception which contains differences between TaskDef registered
        /// and current LHTaskMethod signature.
        /// </summary>
        /// <param name="message"> A custom message.</param>
        /// <param name="innerException">This is another exception that caused this one.</param>
        public LHTaskSchemaMismatchException(string message, Exception innerException) : base(message, innerException) { }
    }
}

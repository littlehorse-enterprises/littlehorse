namespace LittleHorse.Sdk.Exceptions
{
    ///<summary>
    /// Maps Exception that contains object substitutions, parsing or conversion problems. 
    /// </summary>
    public class LHInputVarSubstitutionException : Exception
    {
        ///<summary>
        /// Empty Constructor of the Exception which represents a problem with object substitutions, parsing or conversion. 
        /// </summary>
        public LHInputVarSubstitutionException() : base() { }

        ///<summary>
        /// Constructor of the Exception which represents a problem with object substitutions, parsing or conversion. 
        /// </summary>
        /// <param name="message"> A custom message.</param>
        public LHInputVarSubstitutionException(string message) : base(message) { }

        ///<summary>
        /// Constructor of the Exception which represents a problem with object substitutions, parsing or conversion. 
        /// </summary>
        /// <param name="message"> A custom message.</param>
        /// <param name="innerException">This is another exception that caused this one.</param>
        public LHInputVarSubstitutionException(string message, Exception innerException) : base(message, innerException) { }
    }
}

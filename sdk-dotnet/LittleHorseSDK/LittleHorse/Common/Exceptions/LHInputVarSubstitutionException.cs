namespace LittleHorse.Common.Exceptions
{
    public class LHInputVarSubstitutionException : Exception
    {
        public LHInputVarSubstitutionException() : base() { }

        public LHInputVarSubstitutionException(string message) : base(message) { }

        public LHInputVarSubstitutionException(string message, Exception innerException) : base(message, innerException) { }
    }
}

namespace LittleHorse.Sdk.Exceptions
{
    public class LHMisconfigurationException : Exception
    {
        public LHMisconfigurationException() : base() { }

        public LHMisconfigurationException(string message) : base(message) { }

        public LHMisconfigurationException(string message, Exception innerException) : base(message, innerException) { }
    }
}

namespace LittleHorse.Sdk.Exceptions
{
    public class LHSerdeException : Exception
    {
        public LHSerdeException() : base() { }

        public LHSerdeException(string message) : base(message) { }

        public LHSerdeException(string message, Exception innerException) : base(message, innerException) { }

    }
}

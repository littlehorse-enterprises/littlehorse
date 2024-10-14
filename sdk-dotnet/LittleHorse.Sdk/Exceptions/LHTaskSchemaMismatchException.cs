namespace LittleHorse.Sdk.Exceptions
{
    public class LHTaskSchemaMismatchException : Exception
    {
        public LHTaskSchemaMismatchException() { }

        public LHTaskSchemaMismatchException(string message) : base(message) { }

        public LHTaskSchemaMismatchException(string message, Exception innerException) : base(message, innerException) { }
    }
}

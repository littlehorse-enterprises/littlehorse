namespace LittleHorse.Worker.Attributes
{
    [AttributeUsage(AttributeTargets.Method, AllowMultiple = false)]
    public class LHTaskWorkerAttribute : Attribute
    {
        public string Value;
        public LHTaskWorkerAttribute(string value) 
        { 
            Value = value;
        }
    }
}

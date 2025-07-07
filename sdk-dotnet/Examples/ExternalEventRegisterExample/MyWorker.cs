using LittleHorse.Sdk.Worker;

namespace ExternalEventRegisterExample
{
    public class MyWorker
    {
        public const string CreateDoc = "create-doc";

        [LHTaskMethod(CreateDoc)]
        public async Task<String> GenerateDoc(String input)
        {
            var doc = $"Document created with content: {input}";
            Console.WriteLine(doc);
            return doc.GetHashCode().ToString();
        }
    }
}

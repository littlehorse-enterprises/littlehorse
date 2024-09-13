using Examples.BasicExample;
using Microsoft.Extensions.Configuration;
using LittleHorse.Sdk;
using LittleHorse.Worker;

var props = new Dictionary<string, string>
        {
            { "AppSettings:Setting1", "Value1" }
        };

IConfiguration configuration = new ConfigurationBuilder()
    .AddInMemoryCollection(props)
    .Build();
var config = new LHConfig(configuration);

MyWorker executable = new MyWorker();
var taskWorker = new LHTaskWorker<MyWorker>(executable, "greet-dotnet", config);

taskWorker.RegisterTaskDef();

Thread.Sleep(1000);

taskWorker.Start();

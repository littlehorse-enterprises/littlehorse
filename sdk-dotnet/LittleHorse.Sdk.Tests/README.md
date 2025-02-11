# LittleHorse Tests of Dotnet SDK Project

This readme makes reference on how to run tests in the Dotnet SDK.

It is organized by the same library structure with 2 main folders 

- Worker
- Workflow

There are other support folders:
- Utils
- Helper

When a class has too many functionality to be tested it is recommended to create a new test class to focus on its tests 
like the `WfThreadConditionsSpecTest.cs`. The whole feature about conditionals is in the `WorkflowThread` class.

Finally, to run the tests you should execute the following commands:

```
dotnet build ./LittleHorse.Sdk
dotnet test ./LittleHorse.Sdk.Tests
```
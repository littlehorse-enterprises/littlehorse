using System;
using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Workflow.Spec;
using Xunit;

namespace LittleHorse.Sdk.Tests.Workflow.Spec;

public class ExternalEventNodeOutputTest
{
    public ExternalEventNodeOutputTest()
    {
        LHLoggerFactoryProvider.Initialize(null);
    }

    [Fact]
    public void ToPutExternalEventDefRequest_ShouldSetNameAndContentType()
    {
        var workflow = new Sdk.Workflow.Spec.Workflow("test-external-event", Entrypoint);

        var wfSpec = workflow.Compile();
        var thread = wfSpec.ThreadSpecs[wfSpec.EntrypointThreadName];
        var node = thread.Nodes["1-external-event-def-EXTERNAL_EVENT"];

        Assert.Equal("external-event-def", node.ExternalEvent.ExternalEventDefId.Name);
        return;

        void Entrypoint(WorkflowThread wf)
        {
            wf.WaitForEvent("external-event-def").RegisteredAs(typeof(int));
        }
    }

    [Fact]
    public void ToPutExternalEventDefRequest_ShouldSetContentTypeCorrectly()
    {
        var workflow = new Sdk.Workflow.Spec.Workflow("test-external-event-string", Entrypoint);

        var wfSpec = workflow.Compile();
        var thread = wfSpec.ThreadSpecs[wfSpec.EntrypointThreadName];
        var node = thread.Nodes["1-external-event-def-EXTERNAL_EVENT"];

        Assert.Equal("external-event-def", node.ExternalEvent.ExternalEventDefId.Name);
        return;

        void Entrypoint(WorkflowThread wf)
        {
            wf.WaitForEvent("external-event-def").RegisteredAs(typeof(string));
        }
    }


    [Fact]
    public void WaitForEvent_WithDifferentEventName_ShouldCreateCorrectNode()
    {
        var workflow = new Sdk.Workflow.Spec.Workflow("test-custom-event", Entrypoint);

        var wfSpec = workflow.Compile();
        var thread = wfSpec.ThreadSpecs[wfSpec.EntrypointThreadName];
        var node = thread.Nodes["1-my-custom-event-EXTERNAL_EVENT"];

        Assert.Equal("my-custom-event", node.ExternalEvent.ExternalEventDefId.Name);
        return;

        void Entrypoint(WorkflowThread wf)
        {
            wf.WaitForEvent("my-custom-event").RegisteredAs(typeof(int));
        }
    }
}
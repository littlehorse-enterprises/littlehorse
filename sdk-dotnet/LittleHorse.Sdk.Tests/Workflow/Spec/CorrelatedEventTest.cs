using System;
using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Workflow.Spec;
using Xunit;
using Moq;
using System.Collections.Generic;

namespace LittleHorse.Sdk.Tests.Workflow.Spec;

public class CorrelatedEventTest
{
    public CorrelatedEventTest()
    {
        LHLoggerFactoryProvider.Initialize(null);
    }


    [Fact]
    public void ShouldAutomaticallyMaskCorrelationIdFromMaskedVar()
    {
        void Entrypoint(WorkflowThread wf)
        {
            var ssn = wf.DeclareStr("ssn").Masked();
            var extEvt = wf.WaitForEvent("identity-verified").WithCorrelationId(ssn);
        }
        PutWfSpecRequest workflow = new Sdk.Workflow.Spec.Workflow("example-correlated-event", Entrypoint).Compile();

        ThreadSpec entry = workflow.ThreadSpecs[workflow.EntrypointThreadName];
        Node node = entry.Nodes["1-identity-verified-EXTERNAL_EVENT"];
        Assert.True(node.ExternalEvent.MaskCorrelationKey);
    }

    [Fact]
    public void ShouldNotMaskNormalVariable()
    {
        void Entrypoint(WorkflowThread wf)
        {
            var ssn = wf.DeclareStr("ssn");
            var extEvt = wf.WaitForEvent("identity-verified").WithCorrelationId(ssn);
        }
        PutWfSpecRequest workflow = new Sdk.Workflow.Spec.Workflow("example-correlated-event", Entrypoint).Compile();

        ThreadSpec entry = workflow.ThreadSpecs[workflow.EntrypointThreadName];
        Node node = entry.Nodes["1-identity-verified-EXTERNAL_EVENT"];
        Assert.False(node.ExternalEvent.MaskCorrelationKey);

    }

    [Fact]
    public void ShouldMaskCorrelationIdIfITellItTo()
    {
        void Entrypoint(WorkflowThread wf)
        {
            var ssn = wf.DeclareStr("ssn");
            var extEvt = wf.WaitForEvent("identity-verified").WithCorrelationId(ssn, true);
        }
        PutWfSpecRequest workflow = new Sdk.Workflow.Spec.Workflow("example-correlated-event", Entrypoint).Compile();

        ThreadSpec entry = workflow.ThreadSpecs[workflow.EntrypointThreadName];
        Node node = entry.Nodes["1-identity-verified-EXTERNAL_EVENT"];
        Assert.True(node.ExternalEvent.MaskCorrelationKey);
    }

    [Fact]
    public void ShouldNotMaskCorrelationIdIfITellItNotTo()
    {
        void Entrypoint(WorkflowThread wf)
        {
            var ssn = wf.DeclareStr("ssn");
            var extEvt = wf.WaitForEvent("identity-verified").WithCorrelationId(ssn, false);
        }
        PutWfSpecRequest workflow = new Sdk.Workflow.Spec.Workflow("example-correlated-event", Entrypoint).Compile();

        ThreadSpec entry = workflow.ThreadSpecs[workflow.EntrypointThreadName];
        Node node = entry.Nodes["1-identity-verified-EXTERNAL_EVENT"];
        Assert.False(node.ExternalEvent.MaskCorrelationKey);
    }

}
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

        if (workflow.ThreadSpecs.TryGetValue(workflow.EntrypointThreadName, out var entrypoint))
        {
            if (entrypoint.Nodes.TryGetValue("1-identity-verified-EXTERNAL_EVENT", out var node))
            {
                Assert.True(node.ExternalEvent.MaskCorrelationKey);
            }
            else
            {
                throw new Exception("External Event not found");
            }
        }
        else
        {
            throw new Exception("Entrypoint Thread not found.");
        }

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

        if (workflow.ThreadSpecs.TryGetValue(workflow.EntrypointThreadName, out var entrypoint))
        {
            if (entrypoint.Nodes.TryGetValue("1-identity-verified-EXTERNAL_EVENT", out var node))
            {
                Assert.False(node.ExternalEvent.MaskCorrelationKey);
            }
            else
            {
                throw new Exception("External Event not found");
            }
        }
        else
        {
            throw new Exception("Entrypoint Thread not found.");
        }

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

        if (workflow.ThreadSpecs.TryGetValue(workflow.EntrypointThreadName, out var entrypoint))
        {
            if (entrypoint.Nodes.TryGetValue("1-identity-verified-EXTERNAL_EVENT", out var node))
            {
                Assert.True(node.ExternalEvent.MaskCorrelationKey);
            }
            else
            {
                throw new Exception("External Event not found");
            }
        }
        else
        {
            throw new Exception("Entrypoint Thread not found.");
        }
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

        if (workflow.ThreadSpecs.TryGetValue(workflow.EntrypointThreadName, out var entrypoint))
        {
            if (entrypoint.Nodes.TryGetValue("1-identity-verified-EXTERNAL_EVENT", out var node))
            {
                Assert.False(node.ExternalEvent.MaskCorrelationKey);
            }
            else
            {
                throw new Exception("External Event not found");
            }
        }
        else
        {
            throw new Exception("Entrypoint Thread not found.");
        }
    }

}
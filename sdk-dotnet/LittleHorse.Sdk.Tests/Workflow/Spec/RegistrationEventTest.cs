using System;
using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Workflow.Spec;
using LittleHorse.Sdk.Helper;
using Xunit;

namespace LittleHorse.Sdk.Tests.Workflow.Spec
{
    public class RegistrationEventTest
    {
        public RegistrationEventTest()
        {
            LHLoggerFactoryProvider.Initialize(null);
        }

        [Fact]
        public void ShouldAddEventNameToRequiredEventDefs()
        {
            void Entrypoint(WorkflowThread wf)
            {
                var variable = wf.DeclareInt("myInt");
                wf.ThrowEvent("my-event", variable).RegisteredAs(typeof(int));
            }

            var workflow = new Sdk.Workflow.Spec.Workflow("throw-event-test", Entrypoint);
            workflow.Compile();

            Assert.Contains("my-event", workflow.GetRequiredWorkflowEventDefNames());
        }

        [Fact]
        public void ShouldCreateThrowEventNodeWithCorrectName()
        {
            void Entrypoint(WorkflowThread wf)
            {
                var data = wf.DeclareInt("data");
                wf.ThrowEvent("my-event", data).RegisteredAs(typeof(int));
            }
            
            var workflow = new LittleHorse.Sdk.Workflow.Spec.Workflow("throw-event-node-test", Entrypoint);
            var wfSpec = workflow.Compile();

            var entry = wfSpec.ThreadSpecs[wfSpec.EntrypointThreadName];
            const string expectedNodeKey = "1-throw-my-event-THROW_EVENT";
            Assert.Contains(expectedNodeKey, entry.Nodes.Keys);
            var node = entry.Nodes[expectedNodeKey];
            Assert.Equal("my-event", node.ThrowEvent.EventDefId.Name);
        }

        [Fact]
        public void ShouldPassVariableContentToThrowEvent()
        {
            void Entrypoint(WorkflowThread wf)
            {
                var strVar = wf.DeclareStr("myStr");
                wf.ThrowEvent("string-event", strVar).RegisteredAs(typeof(string));
            }
            
            var workflow = new LittleHorse.Sdk.Workflow.Spec.Workflow("throw-event-content-test", Entrypoint);
            var wfSpec = workflow.Compile();

            var entry = wfSpec.ThreadSpecs[wfSpec.EntrypointThreadName];
            const string expectedNodeKey = "1-throw-string-event-THROW_EVENT";
            Assert.Contains(expectedNodeKey, entry.Nodes.Keys);
            var node = entry.Nodes[expectedNodeKey];
            Assert.Equal(VariableAssignment.SourceOneofCase.VariableName, node.ThrowEvent.Content.SourceCase);
            Assert.Equal("myStr", node.ThrowEvent.Content.VariableName);
        }
        

        [Fact]
        public void ToPutWorkflowEventDefRequest_ShouldMapTypesCorrectly()
        {
            var thread = new MockWorkflowThread();
            
            var intOutput = new ThrowEventNodeOutput("int-event", thread);
            var strOutput = new ThrowEventNodeOutput("str-event", thread); 
            var boolOutput = new ThrowEventNodeOutput("bool-event", thread);
            intOutput.RegisteredAs(typeof(int));
            strOutput.RegisteredAs(typeof(string));
            boolOutput.RegisteredAs(typeof(bool));
            
            var intRequest = intOutput.ToPutWorkflowEventDefRequest();
            var strRequest = strOutput.ToPutWorkflowEventDefRequest();
            var boolRequest = boolOutput.ToPutWorkflowEventDefRequest();
            
            Assert.Equal("int-event", intRequest.Name);
            Assert.Equal("str-event", strRequest.Name);  
            Assert.Equal("bool-event", boolRequest.Name);
            Assert.NotNull(intRequest.ContentType.ReturnType_);
            Assert.NotNull(strRequest.ContentType.ReturnType_);
            Assert.NotNull(boolRequest.ContentType.ReturnType_);
            Assert.Equal(VariableType.Int, intRequest.ContentType.ReturnType_.Type);
            Assert.Equal(VariableType.Str, strRequest.ContentType.ReturnType_.Type);
            Assert.Equal(VariableType.Bool, boolRequest.ContentType.ReturnType_.Type);
        }
        
        [Fact]
        public void ToPutWorkflowEventDefRequest_ShouldThrowExceptionWithNullPayloadType()
        {
            var thread = new MockWorkflowThread();
            var eventOutput = new ThrowEventNodeOutput("null-test-event", thread);
            
            var exception = Assert.Throws<ArgumentException>(() => eventOutput.ToPutWorkflowEventDefRequest());
            
            Assert.Equal("Unsupported payload type for workflow event.", exception.Message);
        }
        
        [Fact]
        public void ToPutWorkflowEventDefRequest_ShouldThrowExceptionWithExplicitNullType()
        {
            var thread = new MockWorkflowThread();
            var eventOutput = new ThrowEventNodeOutput("explicit-null-event", thread);
            
            eventOutput.RegisteredAs(null);
            
            var exception = Assert.Throws<ArgumentException>(() => eventOutput.ToPutWorkflowEventDefRequest());
            Assert.Equal("Unsupported payload type for workflow event.", exception.Message);
        }
        
        [Fact]
        public void ExternalEventAndWorkflowEvent_shouldWorkTogether()
        {
            var workflow = new Sdk.Workflow.Spec.Workflow("test-external-event", Entrypoint);

            var wfSpec = workflow.Compile();
            var thread = wfSpec.ThreadSpecs[wfSpec.EntrypointThreadName];
            var externalEventNode = thread.Nodes["1-doc-name-EXTERNAL_EVENT"];
            var throwEventNode = thread.Nodes["2-throw-create-doc-THROW_EVENT"];
            

            Assert.Equal("doc-name", externalEventNode.ExternalEvent.ExternalEventDefId.Name);
            Assert.Equal("create-doc", throwEventNode.ThrowEvent.EventDefId.Name);
            Assert.Equal("1-doc-name-EXTERNAL_EVENT",throwEventNode.ThrowEvent.Content.NodeOutput.NodeName);
            return;

            void Entrypoint(WorkflowThread wf)
            {
                var docName = wf.WaitForEvent("doc-name").RegisteredAs(typeof(string));
                wf.ThrowEvent("create-doc",docName).RegisteredAs(typeof(string));
            }
        }


        
        
        private class MockWorkflowThread : WorkflowThread
        {
            public MockWorkflowThread() : base(new LittleHorse.Sdk.Workflow.Spec.Workflow("test", _ => {}), _ => {}) { }
        }
    }
}

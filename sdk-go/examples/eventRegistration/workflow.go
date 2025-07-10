package eventRegistration

import (
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
)

func AskForName() string {
	return "What's your name?"
}

func SpecificGreeting(name string) string {
	return "Hello, " + name + "!"
}

const (
	AskForNameTaskName       = "ask-for-name"
	SpecificGreetingTaskName = "specific-greeting"
	EventDefName             = "my-name"
	WorkflowName             = "workflow-and-external-event-registration"
	ThrowEventName           = "greeting-completed"
)

func EventRegistrationWorkflow(wf *littlehorse.WorkflowThread) {
	nameVar := wf.DeclareStr("name")
	wf.Execute(AskForNameTaskName)

	eventOutput := wf.WaitForEvent(EventDefName).RegisteredAs(lhproto.VariableType_STR)

	wf.Mutate(nameVar, lhproto.VariableMutationType_ASSIGN, eventOutput)

	greetingsOutput := wf.Execute(SpecificGreetingTaskName, nameVar)

	wf.ThrowEvent(ThrowEventName, greetingsOutput).RegisteredAs(lhproto.VariableType_STR)
}

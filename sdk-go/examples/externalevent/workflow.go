package externalevent

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
	WorkflowName             = "external-event"
)

func ExternalEventWorkflow(wf *littlehorse.WorkflowThread) {
	nameVar := wf.DeclareStr("name")
	wf.Execute(AskForNameTaskName)

	eventOutput := wf.WaitForEvent(EventDefName)

	wf.Mutate(nameVar, lhproto.VariableMutationType_ASSIGN, eventOutput)

	wf.Execute(SpecificGreetingTaskName, nameVar)
}

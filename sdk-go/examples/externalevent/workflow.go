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

func ExternalEventWorkflow(wf *littlehorse.WorkflowThread) {
	nameVar := wf.AddVariable("name", model.VariableType_STR)
	wf.Execute("ask-for-name")

	eventOutput := wf.WaitForEvent("my-name")

	wf.Mutate(nameVar, model.VariableMutationType_ASSIGN, eventOutput)

	wf.Execute("specific-greeting", nameVar)
}

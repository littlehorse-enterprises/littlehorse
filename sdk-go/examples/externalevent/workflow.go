package externalevent

import (
	"github.com/littlehorse-eng/littlehorse/sdk-go/common/model"
	"github.com/littlehorse-eng/littlehorse/sdk-go/wflib"
)

func AskForName() string {
	return "What's your name?"
}

func SpecificGreeting(name string) string {
	return "Hello, " + name + "!"
}

func ExternalEventWorkflow(thread *wflib.ThreadBuilder) {
	nameVar := thread.AddVariable("name", model.VariableTypePb_STR)
	thread.Execute("ask-for-name")

	eventOutput := thread.WaitForEvent("my-name")

	thread.Mutate(nameVar, model.VariableMutationTypePb_ASSIGN, eventOutput)

	thread.Execute("specific-greeting", nameVar)
}

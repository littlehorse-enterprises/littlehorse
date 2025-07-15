package externalevent

import (
	"fmt"
	"strings"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
)

func AskForName() string {
	return "What's your name?"
}

var counter = 0

func SpecificGreeting(name string) string {
	name = strings.ToUpper(name)
	println("Hello, " + name + "!")
	counter++
	id := strings.ToLower("greeting-" + name + "-" + fmt.Sprint(counter))
	print("Generated ID: " + id)
	return id
}

func ShowSummary(name string, age int) string {
	summary := fmt.Sprintf("Name: %s, Age: %d", strings.ToUpper(name), age)
	if age < 18 {
		summary += " (You are a minor.)"
	} else {
		summary += " (You are an adult.)"
	}
	println("Summary: " + summary)
	return summary
}

const (
	AskForNameTaskName       = "ask-for-name"
	SummaryTaskName          = "show-summary"
	SpecificGreetingTaskName = "specific-greeting"
	EventDefName             = "my-name"
	WorkflowName             = "external-event"
	AgeEventDefName          = "how-old-are-you"
)

func ExternalEventWorkflow(wf *littlehorse.WorkflowThread) {
	nameVar := wf.DeclareStr("name")
	wf.Execute(AskForNameTaskName)

	eventOutput := wf.WaitForEvent(EventDefName) // This event is registered in ./deploy/main.go

	wf.Mutate(nameVar, lhproto.VariableMutationType_ASSIGN, eventOutput)

	greetingId := wf.Execute(SpecificGreetingTaskName, nameVar)
	var age = wf.WaitForEvent(AgeEventDefName).
		SetCorrelationId(greetingId).
		MaskCorrelationId(true).
		WithCorrelatedEventConfig(&lhproto.CorrelatedEventConfig{
			DeleteAfterFirstCorrelation: true,
		}).
		RegisteredAs(lhproto.VariableType_INT) // This event will be registered when the workflow is registered, no need for manual registration.
	wf.Execute(SummaryTaskName, nameVar, age)
}

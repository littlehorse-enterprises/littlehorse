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
	WorkflowName             = "external-event"
	AskForNameTaskName       = "ask-for-name"
	SpecificGreetingTaskName = "specific-greeting"
	NameDefName              = "what-is-your-name"
	AgeEventDefName          = "how-old-are-you"
	AllowSummaryEventDefName = "allow-summary"
	SummaryTaskName          = "show-summary"
)

func ExternalEventWorkflow(wf *littlehorse.WorkflowThread) {

	name := wf.WaitForEvent(NameDefName) // This event is registered in ./deploy/main.go

	greetingId := wf.Execute(SpecificGreetingTaskName, name)

	age := wf.WaitForEvent(AgeEventDefName).
		SetCorrelationId(greetingId). //You can unlock this event by using the ID generated in previous task or using the wfRunId.
		MaskCorrelationId(true).
		WithCorrelatedEventConfig(&lhproto.CorrelatedEventConfig{
			DeleteAfterFirstCorrelation: true,
		}).RegisteredAs(lhproto.VariableType_INT) // This event will be registered when the workflow is registered, no need for manual registration.

	wf.WaitForEvent(AllowSummaryEventDefName).RegisteredAsEmpty() // This external event will be registered without a payload.

	wf.Execute(SummaryTaskName, name, age)
}

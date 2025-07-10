package main

import (
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/examples"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/examples/eventRegistration"
)

func main() {
	_, client := examples.LoadConfigAndClient()
	wf := littlehorse.NewWorkflow(eventRegistration.EventRegistrationWorkflow, eventRegistration.WorkflowName)
	wf.RegisterWfSpec(*client)
}

package main

import (
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"

	examples "github.com/littlehorse-enterprises/littlehorse/examples/go"
	"github.com/littlehorse-enterprises/littlehorse/examples/go/structdef"
)

func main() {
	_, client := examples.LoadConfigAndClient()

	// RegisterWfSpec handles ExternalEventDefs, WorkflowEventDefs, and the WfSpec itself.
	wf := littlehorse.NewWorkflow(structdef.MyWorkflow, structdef.WorkflowName)
	wf.RegisterWfSpec(*client)
}

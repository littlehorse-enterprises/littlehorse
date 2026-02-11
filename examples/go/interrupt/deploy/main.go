package main

import (
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"

	examples "github.com/littlehorse-enterprises/littlehorse/examples/go"
	"github.com/littlehorse-enterprises/littlehorse/examples/go/interrupt"
)

func main() {
	_, client := examples.LoadConfigAndClient()

	wf := littlehorse.NewWorkflow(interrupt.InterruptWorkflow, interrupt.WorkflowName)
	wf.RegisterWfSpec(*client)
}

package main

import (
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"

	examples "github.com/littlehorse-enterprises/littlehorse/examples/go"
	rcw "github.com/littlehorse-enterprises/littlehorse/examples/go/run-child-workflow"
)

func main() {
	_, client := examples.LoadConfigAndClient()

	// Register child then parent using the same names as the Java example
	child := littlehorse.NewWorkflow(rcw.ChildWorkflow, rcw.ChildWorkflowName)
	child.RegisterWfSpec(*client)

	parent := littlehorse.NewWorkflow(rcw.ParentWorkflow, rcw.ParentWorkflowName)
	parent.RegisterWfSpec(*client)
}

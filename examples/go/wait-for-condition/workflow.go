package basic

import (
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
)

const WorkflowName string = "example-wait-for-condition"
const InterruptName string = "subtract"

func WaitForConditionWorkflow(wf *littlehorse.WorkflowThread) {
	counter := wf.DeclareInt("counter").WithDefault(2)

	wf.WaitForCondition(counter.IsEqualTo(0))

	wf.HandleInterrupt(InterruptName, func(handler *littlehorse.WorkflowThread) {
		counter.Assign(counter.Subtract(1))
	})
}

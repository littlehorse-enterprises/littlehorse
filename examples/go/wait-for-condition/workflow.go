package basic

import (
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
)

const WorkflowName string = "example-wait-for-condition"
const InterruptName string = "subtract"

func WaitForConditionWorkflow(wf *littlehorse.WorkflowThread) {
	counter := wf.DeclareInt("counter").WithDefault(2)

	wf.WaitForCondition(wf.Condition(counter, lhproto.Comparator_EQUALS, 0))

	wf.HandleInterrupt(InterruptName, func(handler *littlehorse.WorkflowThread) {
		handler.Mutate(counter, lhproto.VariableMutationType_SUBTRACT, 1)
	})
}

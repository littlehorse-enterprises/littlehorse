package while

import (
	"fmt"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
)

func Donut(num int) int {
	num = num - 1
	fmt.Println("eating another donut, {} left", num)
	return num
}

func DonutWorkflow(wf *littlehorse.WorkflowThread) {
	numDonuts := wf.AddVariable("number-of-donuts", lhproto.VariableType_INT)

	wf.DoWhile(
		wf.Condition(numDonuts, lhproto.Comparator_GREATER_THAN, 0),
		func(t *littlehorse.WorkflowThread) {
			taskOutput := t.Execute("eat-another-donut", numDonuts)
			wf.Mutate(numDonuts, lhproto.VariableMutationType_ASSIGN, taskOutput)
		},
	)
}

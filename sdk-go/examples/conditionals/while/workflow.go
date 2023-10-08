package while

import (
	"fmt"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common/model"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/wflib"
)

func Donut(num int) int {
	num = num - 1
	fmt.Println("eating another donut, {} left", num)
	return num
}

func DonutWorkflow(wf *wflib.WorkflowThread) {
	numDonuts := wf.AddVariable("number-of-donuts", model.VariableType_INT)

	wf.DoWhile(
		wf.Condition(numDonuts, model.Comparator_GREATER_THAN, 0),
		func(t *wflib.WorkflowThread) {
			taskOutput := t.Execute("eat-another-donut", numDonuts)
			wf.Mutate(numDonuts, model.VariableMutationType_ASSIGN, taskOutput)
		},
	)
}

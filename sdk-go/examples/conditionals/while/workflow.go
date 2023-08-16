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

func DonutWorkflow(thread *wflib.ThreadBuilder) {
	numDonuts := thread.AddVariable("number-of-donuts", model.VariableType_INT)

	thread.DoWhile(
		thread.Condition(numDonuts, model.ComparatorPb_GREATER_THAN, 0),
		func(t *wflib.ThreadBuilder) {
			taskOutput := t.Execute("eat-another-donut", numDonuts)
			thread.Mutate(numDonuts, model.VariableMutationTypePb_ASSIGN, taskOutput)
		},
	)
}

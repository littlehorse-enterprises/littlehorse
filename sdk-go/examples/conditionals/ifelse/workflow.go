package ifelse

import (
	"fmt"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
)

func Salad() string {
	fmt.Println("Recommending a salad")
	return "Have a salad!"
}

func Donut() string {
	fmt.Println("Recommending another donut")
	return "Have another donut!"
}

func DonutWorkflow(wf *littlehorse.WorkflowThread) {
	numDonuts := wf.AddVariable("number-of-donuts", lhproto.VariableType_INT)

	wf.DoIfElse(
		wf.Condition(numDonuts, lhproto.Comparator_LESS_THAN, 10),
		func(t *littlehorse.WorkflowThread) {
			t.Execute("eat-another-donut")
		},
		func(t *littlehorse.WorkflowThread) {
			t.Execute("eat-salad")
		},
	)
}

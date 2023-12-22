package ifelse

import (
	"fmt"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common/model"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/wflib"
)

func Salad() string {
	fmt.Println("Recommending a salad")
	return "Have a salad!"
}

func Donut() string {
	fmt.Println("Recommending another donut")
	return "Have another donut!"
}

func DonutWorkflow(wf *wflib.WorkflowThread) {
	numDonuts := wf.AddVariable("number-of-donuts", model.VariableType_INT)

	wf.DoIfElse(
		wf.Condition(numDonuts, model.Comparator_LESS_THAN, 10),
		func(t *wflib.WorkflowThread) {
			t.Mutate(numDonuts, model.VariableMutationType_ASSIGN, 0)
			t.Execute("eat-another-donut")
			t.Mutate(numDonuts, model.VariableMutationType_ASSIGN, 1)
		},
		func(t *wflib.WorkflowThread) {
			t.Mutate(numDonuts, model.VariableMutationType_ASSIGN, 2)
			t.Execute("eat-salad")
			t.Mutate(numDonuts, model.VariableMutationType_ASSIGN, 3)
		},
	)

	wf.Mutate(numDonuts, model.VariableMutationType_ASSIGN, 5)
}

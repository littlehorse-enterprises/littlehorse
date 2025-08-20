package ifelse

import (
	"fmt"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
)

const (
	EatSaladTaskName        string = "eat-salad"
	EatAnotherDonutTaskName string = "eat-another-donut"
	WorkflowName            string = "donut-workflow"
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
	numDonuts := wf.DeclareInt("number-of-donuts")

	wf.DoIf(wf.Condition(numDonuts, lhproto.Comparator_LESS_THAN, 10),
		func(t *littlehorse.WorkflowThread) {
			t.Execute(EatAnotherDonutTaskName)
		}).DoElse(func(t *littlehorse.WorkflowThread) {
		t.Execute(EatSaladTaskName)
	})
}

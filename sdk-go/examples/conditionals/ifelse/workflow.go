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

func DonutWorkflow(thread *wflib.ThreadBuilder) {
	numDonuts := thread.AddVariable("number-of-donuts", model.VariableType_INT)

	thread.DoIfElse(
		thread.Condition(numDonuts, model.ComparatorPb_LESS_THAN, 10),
		func(t *wflib.ThreadBuilder) {
			t.Execute("eat-another-donut")
		},
		func(t *wflib.ThreadBuilder) {
			t.Execute("eat-salad")
		},
	)
}

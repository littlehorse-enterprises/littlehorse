package while

import (
	"fmt"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
)

const (
	TaskDefName  = "eat-another-donut"
	WorkflowName = "donut-workflow"
)

func Donut(num int) int {
	num = num - 1
	fmt.Println("eating another donut, {} left", num)
	return num
}

func DonutWorkflow(wf *littlehorse.WorkflowThread) {
	numDonuts := wf.DeclareInt("number-of-donuts")

	wf.DoWhile(
		numDonuts.IsGreaterThan(0),
		func(t *littlehorse.WorkflowThread) {
			taskOutput := t.Execute(TaskDefName, numDonuts)
			numDonuts.Assign(taskOutput)
		},
	)
}

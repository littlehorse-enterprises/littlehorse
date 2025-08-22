package interrupt

import (
	"strconv"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
)

func ReportTheResult(input int) string {
	return "The final tally was: " + strconv.Itoa(input)
}

func ChildFooTask(interruptInput int) string {
	return "The content of the interrupt was " + strconv.Itoa(interruptInput)
}

const (
	ChildFooTaskName         = "child-foo-task"
	ReportResultTaskName     = "report-the-result"
	WorkflowName             = "interrupt-example"
	UpdateTallyInterruptName = "update-tally"
)

func InterruptWorkflow(wf *littlehorse.WorkflowThread) {
	tally := wf.DeclareInt("tally")
	wf.Execute(ChildFooTaskName, 5)
	wf.Mutate(tally, lhproto.VariableMutationType_ASSIGN, 0)

	// The interrupt handler
	wf.HandleInterrupt(UpdateTallyInterruptName, func(handler *littlehorse.WorkflowThread) {
		eventContent := handler.DeclareInt("INPUT")
		handler.Execute(ChildFooTaskName, eventContent)
		handler.Mutate(tally, lhproto.VariableMutationType_ADD, eventContent)
	})

	// The main thread sleeps for 120 seconds and then reports the tally
	wf.Sleep(120)
	wf.Execute(ReportResultTaskName, tally)
}

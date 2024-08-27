package interrupt

import (
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
	"strconv"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
)

func ReportTheResult(input int) string {
	return "The final tally was: " + strconv.Itoa(input)
}

func ChildFooTask(interruptInput int) string {
	return "The content of the interrupt was " + strconv.Itoa(interruptInput)
}

func InterruptWorkflow(wf *littlehorse.WorkflowThread) {
	tally := wf.AddVariable("tally", lhproto.VariableType_INT)
	wf.Execute("child-foo-task", 5)
	wf.Mutate(tally, lhproto.VariableMutationType_ASSIGN, 0)

	// The interrupt handler
	wf.HandleInterrupt("update-tally", func(handler *littlehorse.WorkflowThread) {
		eventContent := handler.AddVariable("INPUT", lhproto.VariableType_INT)
		handler.Execute("child-foo-task", eventContent)
		handler.Mutate(tally, lhproto.VariableMutationType_ADD, eventContent)
	})

	// The main thread sleeps for 15 seconds and then reports the tally
	wf.Sleep(15)
	wf.Execute("report-the-result", tally)
}

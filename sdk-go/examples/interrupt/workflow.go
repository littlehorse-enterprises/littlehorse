package interrupt

import (
	"strconv"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common/model"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/wflib"
)

func ReportTheResult(input int) string {
	return "The final tally was: " + strconv.Itoa(input)
}

func ChildFooTask(interruptInput int) string {
	return "The content of the interrupt was " + strconv.Itoa(interruptInput)
}

func InterruptWorkflow(wf *wflib.WorkflowThread) {
	tally := wf.AddVariable("tally", model.VariableType_INT)
	wf.Execute("child-foo-task", 5)
	wf.Mutate(tally, model.VariableMutationType_ASSIGN, 0)

	// The interrupt handler
	wf.HandleInterrupt("update-tally", func(handler *wflib.WorkflowThread) {
		eventContent := handler.AddVariable("INPUT", model.VariableType_INT)
		handler.Execute("child-foo-task", eventContent)
		handler.Mutate(tally, model.VariableMutationType_ADD, eventContent)
	})

	// The main thread sleeps for 15 seconds and then reports the tally
	wf.Sleep(15)
	wf.Execute("report-the-result", tally)
}

package interrupt

import (
	"strconv"

	"bitbucket.org/littlehorse-core/littlehorse/sdk-go/common/model"
	"bitbucket.org/littlehorse-core/littlehorse/sdk-go/wflib"
)

func ReportTheResult(input int) string {
	return "The final tally was: " + strconv.Itoa(input)
}

func ChildFooTask(interruptInput int) string {
	return "The content of the interrupt was " + strconv.Itoa(interruptInput)
}

func InterruptWorkflow(thread *wflib.ThreadBuilder) {
	tally := thread.AddVariable("tally", model.VariableTypePb_INT)
	thread.Execute("child-foo-task", 5)
	thread.Mutate(tally, model.VariableMutationTypePb_ASSIGN, 0)

	// The interrupt handler
	thread.HandleInterrupt("update-tally", func(handler *wflib.ThreadBuilder) {
		eventContent := handler.AddVariable("INPUT", model.VariableTypePb_INT)
		handler.Execute("child-foo-task", eventContent)
		handler.Mutate(tally, model.VariableMutationTypePb_ADD, eventContent)
	})

	// The main thread sleeps for 15 seconds and then reports the tally
	thread.Sleep(15)
	thread.Execute("report-the-result", tally)
}

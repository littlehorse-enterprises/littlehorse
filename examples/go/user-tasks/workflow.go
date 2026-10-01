package usertasks

import (
	"fmt"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
)

const (
	WorkflowName  = "it-request"
	EmailTaskName = "send-email"
	ITRequestForm = "it-request"
	ApprovalTask  = "approve-it-request"
)

// SendEmail prints a fake email, like the Java UserTasksExample worker.
func SendEmail(address, content string) {
	fmt.Printf("\n\nSending email to %s\nContent: %s\n", address, content)
}

func MyWorkflow(wf *littlehorse.WorkflowThread) {
	userID := wf.DeclareStr("user-id")
	itRequest := wf.DeclareStruct("it-request", ItemRequestForm{}.LHStructDef().Name)
	isApproved := wf.DeclareBool("is-approved")

	request := wf.AssignUserTask(ITRequestForm, userID, "testGroup")
	wf.ReleaseToGroupOnDeadline(request, 60)
	wf.HandleException(request, nil, func(handler *littlehorse.WorkflowThread) {
		handler.Execute(EmailTaskName, "test-ut-support@gmail.com", "Task cancelled")
	})
	itRequest.Assign(request)

	approval := wf.AssignUserTask(ApprovalTask, nil, "finance").WithNotes(wf.Format(
		"User {0} is requesting to buy item {1}.\nJustification: {2}",
		userID, itRequest.Get("requestedItem"), itRequest.Get("justification"),
	))
	wf.ScheduleReminderTask(approval, 2, EmailTaskName, "finance@gmail.com", "Hi finance team, you have a new assigned task")
	wf.ReassignUserTaskOnDeadline(approval, "test-eduwer", nil, 60)
	isApproved.Assign(approval.Get("isApproved"))

	wf.DoIf(isApproved.IsEqualTo(true), func(body *littlehorse.WorkflowThread) {
		body.Execute(EmailTaskName, userID, body.Format(
			"Dear {0}, your request for {1} has been approved!", userID, itRequest.Get("requestedItem"),
		))
	}).DoElse(func(body *littlehorse.WorkflowThread) {
		body.Execute(EmailTaskName, userID, body.Format(
			"Dear {0}, your request for {1} has been denied.", userID, itRequest.Get("requestedItem"),
		))
	})
}

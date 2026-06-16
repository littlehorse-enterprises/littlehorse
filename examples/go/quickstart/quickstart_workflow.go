package main

import "github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"

const (
	WORKFLOW_NAME                     = "quickstart"
	IDENTITY_VERIFIED_EVENT           = "identity-verified"
	VERIFY_IDENTITY_TASK              = "verify-identity"
	NOTIFY_CUSTOMER_VERIFIED_TASK     = "notify-customer-verified"
	NOTIFY_CUSTOMER_NOT_VERIFIED_TASK = "notify-customer-not-verified"
)

func QuickstartWorkflow(wf *littlehorse.WorkflowThread) {
	fullName := wf.DeclareStr("full-name").Searchable().Required()
	email := wf.DeclareStr("email").Searchable().Required()
	ssn := wf.DeclareInt("ssn").MaskedValue().Required()
	identityVerified := wf.DeclareBool("identity-verified").Searchable()

	wf.Execute(VERIFY_IDENTITY_TASK, fullName, email, ssn).WithRetries(3)

	identityVerificationResult := wf.WaitForEvent(IDENTITY_VERIFIED_EVENT).
		Timeout(60 * 5).
		SetCorrelationId(email)

	exceptionName := littlehorse.Timeout
	wf.HandleError(identityVerificationResult, &exceptionName, func(handler *littlehorse.WorkflowThread) {
		handler.Execute(NOTIFY_CUSTOMER_NOT_VERIFIED_TASK, fullName, email)
		message := "Unable to verify customer identity in time."
		handler.Fail(nil, "customer-not-verified", &message)
	})

	identityVerified.Assign(identityVerificationResult)

	wf.DoIfElse(
		identityVerified.IsEqualTo(true),
		func(ifBody *littlehorse.WorkflowThread) {
			ifBody.Execute(NOTIFY_CUSTOMER_VERIFIED_TASK, fullName, email)
		},
		func(elseBody *littlehorse.WorkflowThread) {
			elseBody.Execute(NOTIFY_CUSTOMER_NOT_VERIFIED_TASK, fullName, email)
		},
	)
}

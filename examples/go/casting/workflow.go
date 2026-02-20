package casting

import (
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
)

const WorkflowName = "casting-workflow"

const (
	StringTask = "string-method"
	IntTask    = "int-method"
	DoubleTask = "double-method"
	BoolTask   = "bool-method"
)

func MyWorkflow(wf *littlehorse.WorkflowThread) {
	stringInput := wf.DeclareStr("string-number").WithDefault("3.14")
	stringBool := wf.DeclareStr("string-bool").WithDefault("false")
	jsonInput := wf.DeclareJsonObj("json-input").WithDefault(map[string]interface{}{"int": "1", "string": "hello"})

	doubleResult := wf.Execute(DoubleTask, stringInput.CastToDouble()) // Manual cast STR -> DOUBLE
	intResult := wf.Execute(IntTask, doubleResult.CastToInt())         // Manual cast DOUBLE -> INT

	mathOverDouble := doubleResult.Multiply(2.0).Divide(6.0) // DOUBLE expression
	wf.Execute(IntTask, mathOverDouble.CastToInt())          // Cast expression -> INT

	boolResult := wf.Execute(BoolTask, stringBool.CastToBool()) // STR -> BOOL
	wf.HandleError(boolResult, nil, func(t *littlehorse.WorkflowThread) {
		// Example error handler if cast fails
		t.Execute(StringTask, "This is how to handle casting errors")
	})

	wf.Execute(IntTask, doubleResult.CastToInt()) // DOUBLE -> INT (explicit)
	wf.Execute(DoubleTask, intResult)             // INT -> DOUBLE (auto)
	wf.Execute(IntTask, jsonInput.JsonPath("$.int").CastToInt())
	wf.Execute(StringTask, stringInput)
}

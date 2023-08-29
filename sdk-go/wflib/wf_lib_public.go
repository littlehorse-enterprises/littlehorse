package wflib

import (
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common/model"
)

type ThreadFunc func(*ThreadBuilder)

func NewWorkflow(threadFunc ThreadFunc, name string) *LHWorkflow {
	return &LHWorkflow{
		Name:             name,
		EntrypointThread: threadFunc,
	}
}

type LHWorkflow struct {
	EntrypointThread ThreadFunc
	Name             string

	spec  model.PutWfSpecRequest
	funcs map[string]ThreadFunc
}

type ThreadBuilder struct {
	Name              string
	isActive          bool
	spec              model.ThreadSpec
	wf                *LHWorkflow
	lastNodeName      *string
	lastNodeCondition *WorkflowCondition
}

type WfRunVariable struct {
	Name    string
	VarType *model.VariableType

	thread   *ThreadBuilder
	jsonPath *string
}

type NodeOutput struct {
	nodeName string
	jsonPath *string
	thread   *ThreadBuilder
}

type WorkflowCondition struct {
	spec          *model.EdgeCondition
	createdAtNode string
}

type SpawnedThread struct {
	thread       *ThreadBuilder
	threadNumVar *WfRunVariable
}

func (n *NodeOutput) JsonPath(path string) NodeOutput {
	return n.jsonPathImpl(path)
}

func (w *WfRunVariable) JsonPath(path string) WfRunVariable {
	return w.jsonPathImpl(path)
}

func (l *LHWorkflow) Compile() (*model.PutWfSpecRequest, error) {
	return l.compile()
}

func (t *ThreadBuilder) AddVariable(
	name string, varType model.VariableType,
) *WfRunVariable {
	return t.addVariable(name, varType, nil)
}

func (t *ThreadBuilder) AddVariableWithDefault(
	name string, varType model.VariableType, defaultValue interface{},
) *WfRunVariable {
	return t.addVariable(name, varType, defaultValue)
}

func (t *ThreadBuilder) Execute(name string, args ...interface{}) NodeOutput {
	return t.executeTask(name, args)
}

func (t *ThreadBuilder) Mutate(
	lhs *WfRunVariable,
	mutation model.VariableMutationType,
	rhs interface{},
) {
	t.mutate(lhs, mutation, rhs)
}

func (t *ThreadBuilder) Condition(
	lhs interface{}, op model.Comparator, rhs interface{},
) *WorkflowCondition {
	return t.condition(lhs, op, rhs)
}

type IfElseBody func(t *ThreadBuilder)

func (t *ThreadBuilder) DoIf(cond *WorkflowCondition, doIf IfElseBody) {
	t.doIf(cond, doIf)
}

func (t *ThreadBuilder) DoIfElse(cond *WorkflowCondition, doIf IfElseBody, doElse IfElseBody) {
	t.doIfElse(cond, doIf, doElse)
}

func (t *ThreadBuilder) DoWhile(cond *WorkflowCondition, whileBody ThreadFunc) {
	t.doWhile(cond, whileBody)
}

func (t *ThreadBuilder) SpawnThread(
	tFunc ThreadFunc, threadName string, args map[string]interface{},
) *SpawnedThread {
	return t.spawnThread(tFunc, threadName, args)
}

func (t *ThreadBuilder) WaitForThreads(s ...*SpawnedThread) NodeOutput {
	return *t.waitForThreads(s...)
}

func (t *ThreadBuilder) WaitForEvent(eventName string) NodeOutput {
	return *t.waitForEvent(eventName)
}

func (t *ThreadBuilder) Sleep(sleepSeconds int) {
	t.sleep(sleepSeconds)
}

func (t *ThreadBuilder) Fail(content interface{}, failureName string, msg *string) {
	t.fail(content, failureName, msg)
}

func (t *ThreadBuilder) HandleInterrupt(interruptName string, handler ThreadFunc) {
	t.handleInterrupt(interruptName, handler)
}

func (t *ThreadBuilder) HandleException(
	nodeOutput *NodeOutput,
	exceptionName *string,
	handler ThreadFunc,
) {
	t.handleException(nodeOutput, exceptionName, handler)
}

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
	varDef   *model.VariableDef
}

type NodeOutput struct {
	nodeName string
	jsonPath *string
	thread   *ThreadBuilder
}

type UserTaskOutput struct {
	Output NodeOutput
	thread *ThreadBuilder
	node   *model.Node
}

type WorkflowCondition struct {
	spec          *model.EdgeCondition
	createdAtNode string
}

type SpawnedThread struct {
	thread       *ThreadBuilder
	threadNumVar *WfRunVariable
}

type SpawnedThreads struct {
	thread     *ThreadBuilder
	threadsVar *WfRunVariable
}

type LHFormatString struct {
	format     string
	formatArgs []*WfRunVariable
	thread     *ThreadBuilder
}

type LHErrorType string

const (
	ChildFailure      LHErrorType = "CHILD_FAILURE"
	VarSubError       LHErrorType = "VAR_SUB_ERROR"
	VarMutationError  LHErrorType = "VAR_MUTATION_ERROR"
	UserTaskCancelled LHErrorType = "USER_TASK_CANCELLED"
	Timeout           LHErrorType = "TIMEOUT"
	TaskFailure       LHErrorType = "TASK_FAILURE"
	VarError          LHErrorType = "VAR_ERROR"
	TaskError         LHErrorType = "TASK_ERROR"
	InternalError     LHErrorType = "INTERNAL_ERROR"
)

func (n *NodeOutput) JsonPath(path string) NodeOutput {
	return n.jsonPathImpl(path)
}

func (w *WfRunVariable) JsonPath(path string) WfRunVariable {
	return w.jsonPathImpl(path)
}

func (w *WfRunVariable) WithIndex(indexType model.IndexType) *WfRunVariable {
	w.setIndex(indexType)
	return w
}

func (w *WfRunVariable) Persistent() *WfRunVariable {
	w.varDef.Persistent = true
	return w
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

func (t *ThreadBuilder) SpawnThreadForEach(
	arrVar *WfRunVariable, threadName string, threadFunc ThreadFunc, args *map[string]interface{},
) *SpawnedThreads {
	return t.spawnThreadForEach(arrVar, threadName, threadFunc, args)
}

func (t *ThreadBuilder) WaitForThreadsList(s *SpawnedThreads) NodeOutput {
	return t.waitForThreadsList(s)
}

func (t *ThreadBuilder) AssignUserTask(
	userTaskDefName string, userId, userGroup interface{},
) *UserTaskOutput {
	return t.assignUserTask(userTaskDefName, userId, userGroup)
}

func (t *ThreadBuilder) Format(format string, args ...*WfRunVariable) *LHFormatString {
	return t.format(format, args)
}

func (t *ThreadBuilder) ScheduleReminderTask(
	userTask *UserTaskOutput, delaySeconds interface{},
	taskDefName string, args ...interface{},
) {
	t.scheduleReminderTask(userTask, delaySeconds, taskDefName, args)
}

func (t *ThreadBuilder) ReleaseToGroupOnDeadline(
	userTask *UserTaskOutput, deadlineSeconds interface{},
) {
	t.releaseToGroupOnDeadline(userTask, deadlineSeconds)
}

func (t *ThreadBuilder) ReassignUserTaskOnDeadline(
	userTask *UserTaskOutput, userId, userGroup, deadlineSeconds interface{},
) {
	t.reassignUserTaskOnDeadline(userTask, userId, userGroup, deadlineSeconds)
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

func (t *ThreadBuilder) HandleError(
	nodeOutput *NodeOutput,
	specificError *LHErrorType,
	handler ThreadFunc,
) {
	t.handleError(nodeOutput, specificError, handler)
}

func (t *ThreadBuilder) HandleException(
	nodeOutput *NodeOutput,
	exceptionName *string,
	handler ThreadFunc,
) {
	t.handleException(nodeOutput, exceptionName, handler)
}

func (t *ThreadBuilder) HandleAnyFailure(
	nodeOutput *NodeOutput,
	handler ThreadFunc,
) {
	t.handleAnyFailure(nodeOutput, handler)
}

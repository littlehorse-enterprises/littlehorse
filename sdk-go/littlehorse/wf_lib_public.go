package littlehorse

import (
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
)

type ThreadFunc func(*WorkflowThread)

func NewWorkflow(threadFunc ThreadFunc, name string) *LHWorkflow {
	return &LHWorkflow{
		Name:             name,
		EntrypointThread: threadFunc,
	}
}

type LHWorkflow struct {
	EntrypointThread ThreadFunc
	Name             string

	spec  lhproto.PutWfSpecRequest
	funcs map[string]ThreadFunc
}

type WorkflowThread struct {
	Name              string
	isActive          bool
	spec              lhproto.ThreadSpec
	wf                *LHWorkflow
	lastNodeName      *string
	lastNodeCondition *WorkflowCondition
	variableMutations []*lhproto.VariableMutation
}

type WfRunVariable struct {
	Name    string
	VarType *lhproto.VariableType

	thread       *WorkflowThread
	jsonPath     *string
	threadVarDef *lhproto.ThreadVarDef
}

type NodeOutput struct {
	nodeName string
	jsonPath *string
	thread   *WorkflowThread
}

type UserTaskOutput struct {
	Output NodeOutput
	thread *WorkflowThread
	node   *lhproto.Node
}

type WorkflowCondition struct {
	spec *lhproto.EdgeCondition
}

type SpawnedThread struct {
	thread       *WorkflowThread
	threadNumVar *WfRunVariable
}

type SpawnedThreads struct {
	thread     *WorkflowThread
	threadsVar *WfRunVariable
}

type LHFormatString struct {
	format     string
	formatArgs []*WfRunVariable
	thread     *WorkflowThread
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

func (n *NodeOutput) HandleExceptionOnChild(handler ThreadFunc, exceptionName *string) {
	n.handleExceptionOnChild(handler, exceptionName)
}

func (n *NodeOutput) HandleErrorOnChild(handler ThreadFunc, errorName *string) {
	n.handleErrorOnChild(handler, errorName)
}

func (n *NodeOutput) HandleAnyFailureOnChild(handler ThreadFunc) {
	n.handleAnyFailureOnChild(handler)
}

func (w *WfRunVariable) JsonPath(path string) WfRunVariable {
	return w.jsonPathImpl(path)
}

func (w *WfRunVariable) WithAccessLevel(accessLevel lhproto.WfRunVariableAccessLevel) WfRunVariable {
	return w.withAccessLevel(accessLevel)
}

func (w *WfRunVariable) AsPublic() WfRunVariable {
	return w.withAccessLevel(lhproto.WfRunVariableAccessLevel_PUBLIC_VAR)
}

func (w *WfRunVariable) AsInherited() WfRunVariable {
	return w.withAccessLevel(lhproto.WfRunVariableAccessLevel_INHERITED_VAR)
}

func (w *WfRunVariable) Searchable() *WfRunVariable {
	return w.searchableImpl()
}

func (w *WfRunVariable) SearchableOn(fieldPath string, fieldType lhproto.VariableType) *WfRunVariable {
	return w.searchableOnImpl(fieldPath, fieldType)
}

func (w *WfRunVariable) MaskedValue() *WfRunVariable {
	return w.maskedValueImpl()
}

func (w *WfRunVariable) Required() *WfRunVariable {
	return w.requiredImpl()
}

func (l *LHWorkflow) WithRetentionPolicy(policy *lhproto.WorkflowRetentionPolicy) *LHWorkflow {
	l.spec.RetentionPolicy = policy
	return l
}

func (l *LHWorkflow) Compile() (*lhproto.PutWfSpecRequest, error) {
	return l.compile()
}

func (l *LHWorkflow) WithUpdateType(updateType lhproto.AllowedUpdateType) *LHWorkflow {
	l.spec.AllowedUpdates = updateType
	return l
}

func (t *WorkflowThread) AddVariable(
	name string, varType lhproto.VariableType,
) *WfRunVariable {
	return t.addVariable(name, varType, nil)
}

func (t *WorkflowThread) WithRetentionPolicy(policy *lhproto.ThreadRetentionPolicy) {
	t.spec.RetentionPolicy = policy
}

func (t *WorkflowThread) AddVariableWithDefault(
	name string, varType lhproto.VariableType, defaultValue interface{},
) *WfRunVariable {
	return t.addVariable(name, varType, defaultValue)
}

func (t *WorkflowThread) Execute(taskDefName interface{}, args ...interface{}) NodeOutput {
	return t.executeTask(taskDefName, args)
}

func (t *WorkflowThread) Mutate(
	lhs *WfRunVariable,
	mutation lhproto.VariableMutationType,
	rhs interface{},
) {
	t.mutate(lhs, mutation, rhs)
}

func (t *WorkflowThread) Condition(
	lhs interface{}, op lhproto.Comparator, rhs interface{},
) *WorkflowCondition {
	return t.condition(lhs, op, rhs)
}

func (t *WorkflowThread) ThrowEvent(workflowEventDefName string, content interface{}) {
	t.throwEvent(workflowEventDefName, content)
}

type IfElseBody func(t *WorkflowThread)

func (t *WorkflowThread) DoIf(cond *WorkflowCondition, doIf IfElseBody) {
	t.doIf(cond, doIf)
}

func (t *WorkflowThread) DoIfElse(cond *WorkflowCondition, doIf IfElseBody, doElse IfElseBody) {
	t.doIfElse(cond, doIf, doElse)
}

func (t *WorkflowThread) DoWhile(cond *WorkflowCondition, whileBody ThreadFunc) {
	t.doWhile(cond, whileBody)
}

func (t *WorkflowThread) SpawnThread(
	tFunc ThreadFunc, threadName string, args map[string]interface{},
) *SpawnedThread {
	return t.spawnThread(tFunc, threadName, args)
}

func (t *WorkflowThread) WaitForThreads(s ...*SpawnedThread) NodeOutput {
	return *t.waitForThreads(s...)
}

func (t *WorkflowThread) SpawnThreadForEach(
	arrVar *WfRunVariable, threadName string, threadFunc ThreadFunc, args *map[string]interface{},
) *SpawnedThreads {
	return t.spawnThreadForEach(arrVar, threadName, threadFunc, args)
}

func (t *WorkflowThread) WaitForThreadsList(s *SpawnedThreads) NodeOutput {
	return t.waitForThreadsList(s)
}

func (t *WorkflowThread) AssignUserTask(
	userTaskDefName string, userId, userGroup interface{},
) *UserTaskOutput {
	return t.assignUserTask(userTaskDefName, userId, userGroup)
}

func (t *WorkflowThread) Format(format string, args ...*WfRunVariable) *LHFormatString {
	return t.format(format, args)
}

func (t *WorkflowThread) CancelUserTaskAfter(userTask *UserTaskOutput, delaySeconds interface{}) {
	t.cancelUserTaskAfter(userTask, delaySeconds)
}

func (t *WorkflowThread) CancelUserTaskAfterAssignment(userTask *UserTaskOutput, delaySeconds interface{}) {
	t.cancelUserTaskAfterAssignment(userTask, delaySeconds)
}

func (t *WorkflowThread) ScheduleReminderTask(
	userTask *UserTaskOutput, delaySeconds interface{},
	taskDefName string, args ...interface{},
) {
	t.scheduleReminderTask(userTask, delaySeconds, taskDefName, args)
}

func (t *WorkflowThread) ScheduleReminderTaskOnAssignment(
	userTask *UserTaskOutput, delaySeconds interface{},
	taskDefName string, args ...interface{},
) {
	t.scheduleReminderTaskOnAssignment(userTask, delaySeconds, taskDefName, args)
}

func (t *WorkflowThread) ReleaseToGroupOnDeadline(
	userTask *UserTaskOutput, deadlineSeconds interface{},
) {
	t.releaseToGroupOnDeadline(userTask, deadlineSeconds)
}

func (t *WorkflowThread) ReassignUserTaskOnDeadline(
	userTask *UserTaskOutput, userId, userGroup, deadlineSeconds interface{},
) {
	t.reassignUserTaskOnDeadline(userTask, userId, userGroup, deadlineSeconds)
}

func (t *WorkflowThread) WaitForEvent(eventName string) NodeOutput {
	return *t.waitForEvent(eventName)
}

func (t *WorkflowThread) Sleep(sleepSeconds int) {
	t.sleep(sleepSeconds)
}

func (t *WorkflowThread) Fail(content interface{}, failureName string, msg *string) {
	t.fail(content, failureName, msg)
}

func (t *WorkflowThread) HandleInterrupt(interruptName string, handler ThreadFunc) {
	t.handleInterrupt(interruptName, handler)
}

func (t *WorkflowThread) HandleError(
	nodeOutput *NodeOutput,
	specificError *LHErrorType,
	handler ThreadFunc,
) {
	t.handleError(nodeOutput, specificError, handler)
}

func (t *WorkflowThread) HandleException(
	nodeOutput *NodeOutput,
	exceptionName *string,
	handler ThreadFunc,
) {
	t.handleException(nodeOutput, exceptionName, handler)
}

func (t *WorkflowThread) HandleAnyFailure(
	nodeOutput *NodeOutput,
	handler ThreadFunc,
) {
	t.handleAnyFailure(nodeOutput, handler)
}

func (u *UserTaskOutput) WithNotes(notes interface{}) *UserTaskOutput {
	userTaskNode := u.node.GetUserTask()
	notesVar, err := u.thread.assignVariable(notes)

	if err != nil {
		u.thread.throwError(err)
	}
	userTaskNode.Notes = notesVar
	return u
}

func (u *UserTaskOutput) WithOnCancellationException(exceptionName interface{}) *UserTaskOutput {
	userTaskNode := u.node.GetUserTask()
	onCancellationExceptionName, err := u.thread.assignVariable(exceptionName)

	if err != nil {
		u.thread.throwError(err)
	}
	userTaskNode.OnCancellationExceptionName = onCancellationExceptionName
	return u
}

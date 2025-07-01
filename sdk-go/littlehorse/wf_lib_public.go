package littlehorse

import (
	"errors"

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
	threads          []*WorkflowThread

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

type NodeOutput interface {
	getNodeName() string
	getJsonPath() *string
	getThread() *WorkflowThread

	JsonPath(string) NodeOutput
}

type WaitForThreadsNodeOutput struct {
	nodeName string
	jsonPath *string
	thread   *WorkflowThread
}

func (w *WaitForThreadsNodeOutput) JsonPath(jsonPath string) NodeOutput {
	return &WaitForThreadsNodeOutput{
		nodeName: w.nodeName,
		jsonPath: &jsonPath,
		thread:   w.thread,
	}
}

type TaskNodeOutput struct {
	nodeName string
	jsonPath *string
	thread   *WorkflowThread
}

func (n *TaskNodeOutput) JsonPath(jsonPath string) NodeOutput {
	return &TaskNodeOutput{
		nodeName: n.nodeName,
		jsonPath: &jsonPath,
		thread:   n.thread,
	}
}

type ExternalEventNodeOutput struct {
	nodeName string
	jsonPath *string
	thread   *WorkflowThread
}

func (n *ExternalEventNodeOutput) JsonPath(jsonPath string) NodeOutput {
	return &ExternalEventNodeOutput{
		nodeName: n.nodeName,
		jsonPath: &jsonPath,
		thread:   n.thread,
	}
}

func (n *ExternalEventNodeOutput) Timeout(timeout int64) *ExternalEventNodeOutput {
	n.thread.addTimeoutToExtEvtNode(n, timeout)
	return n
}

func (n *ExternalEventNodeOutput) SetCorrelationId(id interface{}) *ExternalEventNodeOutput {
	n.thread.setCorrelationId(n, id)
	return n
}

func (n *TaskNodeOutput) Timeout(timeout int64) *TaskNodeOutput {
	n.thread.addTimeoutToTaskNode(n, timeout)
	return n
}

type UserTaskNodeOutput struct {
	nodeName string
	jsonPath *string
	thread   *WorkflowThread
	node     *lhproto.Node
}

func (n *UserTaskNodeOutput) JsonPath(jsonPath string) *UserTaskNodeOutput {
	return &UserTaskNodeOutput{
		nodeName: n.nodeName,
		jsonPath: &jsonPath,
		thread:   n.thread,
	}
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

type WorkflowIfStatement struct {
	firstNopNodeName string
	lastNopNodeName  string
	wasElseExecuted  bool
	thread           *WorkflowThread
}

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

type LHExpression interface {
	Add(other interface{}) LHExpression
	Subtract(other interface{}) LHExpression
	Multiply(other interface{}) LHExpression
	Divide(other interface{}) LHExpression
	Extend(other interface{}) LHExpression
	RemoveIfPresent(other interface{}) LHExpression
	RemoveIndex_ByInt(index int) LHExpression
	RemoveIndex_ByExpression(index LHExpression) LHExpression
	RemoveKey(key interface{}) LHExpression
}

// func (n *PlainNodeOutput) Add(other interface{}) LHExpression {
// 	return LHExpression{
// 		lhs:       n,
// 		rhs:       other,
// 		operation: lhproto.VariableMutationType_ADD,
// 	}
// }

// func (n *PlainNodeOutput) Subtract(other interface{}) LHExpression {
// 	return LHExpression{
// 		lhs:       n,
// 		rhs:       other,
// 		operation: lhproto.VariableMutationType_SUBTRACT,
// 	}
// }

// func (n *PlainNodeOutput) Multiply(other interface{}) LHExpression {
// 	return LHExpression{
// 		lhs:       n,
// 		rhs:       other,
// 		operation: lhproto.VariableMutationType_MULTIPLY,
// 	}
// }

// func (n *PlainNodeOutput) Divide(other interface{}) LHExpression {
// 	return LHExpression{
// 		lhs:       n,
// 		rhs:       other,
// 		operation: lhproto.VariableMutationType_DIVIDE,
// 	}
// }

// func (n *PlainNodeOutput) Extend(other interface{}) LHExpression {
// 	return LHExpression{
// 		lhs:       n,
// 		rhs:       other,
// 		operation: lhproto.VariableMutationType_EXTEND,
// 	}
// }

// func (n *PlainNodeOutput) RemoveIfPresent(other interface{}) LHExpression {
// 	return LHExpression{
// 		lhs:       n,
// 		rhs:       other,
// 		operation: lhproto.VariableMutationType_REMOVE_IF_PRESENT,
// 	}
// }

// func (n *PlainNodeOutput) RemoveIndex_ByInt(index int) LHExpression {
// 	return LHExpression{
// 		lhs:       n,
// 		rhs:       index,
// 		operation: lhproto.VariableMutationType_REMOVE_INDEX,
// 	}
// }

// func (n *PlainNodeOutput) RemoveIndex_ByExpression(index LHExpression) LHExpression {
// 	return LHExpression{
// 		lhs:       n,
// 		rhs:       index,
// 		operation: lhproto.VariableMutationType_REMOVE_INDEX,
// 	}
// }

// func (n *PlainNodeOutput) RemoveKey(key interface{}) LHExpression {
// 	return LHExpression{
// 		lhs:       n,
// 		rhs:       key,
// 		operation: lhproto.VariableMutationType_REMOVE_KEY,
// 	}
// }

func (n *WaitForThreadsNodeOutput) HandleExceptionOnChild(handler ThreadFunc, exceptionName *string) {
	n.handleExceptionOnChild(handler, exceptionName)
}

func (n *WaitForThreadsNodeOutput) HandleErrorOnChild(handler ThreadFunc, errorName *string) {
	n.handleErrorOnChild(handler, errorName)
}

func (n *WaitForThreadsNodeOutput) HandleAnyFailureOnChild(handler ThreadFunc) {
	n.handleAnyFailureOnChild(handler)
}

func (t *TaskNodeOutput) WithRetries(retries int32) *TaskNodeOutput {
	return t.withRetriesImpl(retries)
}

func (t *TaskNodeOutput) WithExponentialBackoff(policy *lhproto.ExponentialBackoffRetryPolicy) *TaskNodeOutput {
	return t.withExponentialBackoffImpl(policy)
}

func (w *WfRunVariable) WithDefault(defaultValue interface{}) *WfRunVariable {
	return w.withDefaultImpl(defaultValue)
}

func (w *WfRunVariable) JsonPath(path string) *WfRunVariable {
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

func (w *WfRunVariable) IsEqualTo(rhs interface{}) *WorkflowCondition {
	return w.thread.condition(w, lhproto.Comparator_EQUALS, rhs)
}

func (w *WfRunVariable) IsNotEqualTo(rhs interface{}) *WorkflowCondition {
	return w.thread.condition(w, lhproto.Comparator_NOT_EQUALS, rhs)
}

func (w *WfRunVariable) IsGreaterThan(rhs interface{}) *WorkflowCondition {
	return w.thread.condition(w, lhproto.Comparator_GREATER_THAN, rhs)
}

func (w *WfRunVariable) IsGreaterThanEq(rhs interface{}) *WorkflowCondition {
	return w.thread.condition(w, lhproto.Comparator_GREATER_THAN_EQ, rhs)
}

func (w *WfRunVariable) IsLessThan(rhs interface{}) *WorkflowCondition {
	return w.thread.condition(w, lhproto.Comparator_LESS_THAN, rhs)
}

func (w *WfRunVariable) IsLessThanEq(rhs interface{}) *WorkflowCondition {
	return w.thread.condition(w, lhproto.Comparator_LESS_THAN_EQ, rhs)
}

func (w *WfRunVariable) DoesContain(rhs interface{}) *WorkflowCondition {
	return w.thread.condition(rhs, lhproto.Comparator_IN, w)
}

func (w *WfRunVariable) DoesNotContain(rhs interface{}) *WorkflowCondition {
	return w.thread.condition(rhs, lhproto.Comparator_NOT_IN, w)
}

func (w *WfRunVariable) IsIn(rhs interface{}) *WorkflowCondition {
	return w.thread.condition(w, lhproto.Comparator_IN, rhs)
}

func (w *WfRunVariable) IsNotIn(rhs interface{}) *WorkflowCondition {
	return w.thread.condition(w, lhproto.Comparator_NOT_IN, rhs)
}

func (w *WfRunVariable) Assign(rhs interface{}) {
	activeThread := w.thread
	lastThread := w.thread.wf.threads[len(w.thread.wf.threads)-1]

	if lastThread.isActive {
		activeThread = lastThread
	}

	activeThread.mutate(w, lhproto.VariableMutationType_ASSIGN, rhs)
}

func (w *WfRunVariable) Add(other interface{}) LHExpression {
	return &lhExpression{
		lhs:       w,
		rhs:       other,
		operation: lhproto.VariableMutationType_ADD,
	}
}

func (w *WfRunVariable) Subtract(other interface{}) LHExpression {
	return &lhExpression{
		lhs:       w,
		rhs:       other,
		operation: lhproto.VariableMutationType_SUBTRACT,
	}
}

func (w *WfRunVariable) Multiply(other interface{}) LHExpression {
	return &lhExpression{
		lhs:       w,
		rhs:       other,
		operation: lhproto.VariableMutationType_MULTIPLY,
	}
}

func (w *WfRunVariable) Divide(other interface{}) LHExpression {
	return &lhExpression{
		lhs:       w,
		rhs:       other,
		operation: lhproto.VariableMutationType_DIVIDE,
	}
}

func (w *WfRunVariable) Extend(other interface{}) LHExpression {
	return &lhExpression{
		lhs:       w,
		rhs:       other,
		operation: lhproto.VariableMutationType_EXTEND,
	}
}

func (w *WfRunVariable) RemoveIfPresent(other interface{}) LHExpression {
	return &lhExpression{
		lhs:       w,
		rhs:       other,
		operation: lhproto.VariableMutationType_REMOVE_IF_PRESENT,
	}
}

func (w *WfRunVariable) RemoveIndex_ByInt(index int) LHExpression {
	return &lhExpression{
		lhs:       w,
		rhs:       index,
		operation: lhproto.VariableMutationType_REMOVE_INDEX,
	}
}

func (w *WfRunVariable) RemoveIndex_ByExpression(index LHExpression) LHExpression {
	return &lhExpression{
		lhs:       w,
		rhs:       index,
		operation: lhproto.VariableMutationType_REMOVE_INDEX,
	}
}

func (w *WfRunVariable) RemoveKey(key interface{}) LHExpression {
	return &lhExpression{
		lhs:       w,
		rhs:       key,
		operation: lhproto.VariableMutationType_REMOVE_KEY,
	}
}

func (t *WorkflowThread) DeclareBool(name string) *WfRunVariable {
	return t.addVariable(name, lhproto.VariableType_BOOL)
}

func (t *WorkflowThread) DeclareInt(name string) *WfRunVariable {
	return t.addVariable(name, lhproto.VariableType_INT)
}

func (t *WorkflowThread) DeclareStr(name string) *WfRunVariable {
	return t.addVariable(name, lhproto.VariableType_STR)
}

func (t *WorkflowThread) DeclareDouble(name string) *WfRunVariable {
	return t.addVariable(name, lhproto.VariableType_DOUBLE)
}

func (t *WorkflowThread) DeclareBytes(name string) *WfRunVariable {
	return t.addVariable(name, lhproto.VariableType_BYTES)
}

func (t *WorkflowThread) DeclareJsonArr(name string) *WfRunVariable {
	return t.addVariable(name, lhproto.VariableType_JSON_ARR)
}

func (t *WorkflowThread) DeclareJsonObj(name string) *WfRunVariable {
	return t.addVariable(name, lhproto.VariableType_JSON_OBJ)
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
	return t.addVariable(name, varType)
}

func (t *WorkflowThread) Add(lhs interface{}, rhs interface{}) lhExpression {
	return lhExpression{
		lhs:       lhs,
		rhs:       rhs,
		operation: lhproto.VariableMutationType_ADD,
	}
}

func (t *WorkflowThread) Subtract(lhs interface{}, rhs interface{}) lhExpression {
	return lhExpression{
		lhs:       lhs,
		rhs:       rhs,
		operation: lhproto.VariableMutationType_SUBTRACT,
	}
}

func (t *WorkflowThread) Multiply(lhs interface{}, rhs interface{}) lhExpression {
	return lhExpression{
		lhs:       lhs,
		rhs:       rhs,
		operation: lhproto.VariableMutationType_MULTIPLY,
	}
}

func (t *WorkflowThread) Divide(lhs interface{}, rhs interface{}) lhExpression {
	return lhExpression{
		lhs:       lhs,
		rhs:       rhs,
		operation: lhproto.VariableMutationType_DIVIDE,
	}
}

func (t *WorkflowThread) Extend(lhs interface{}, rhs interface{}) lhExpression {
	return lhExpression{
		lhs:       lhs,
		rhs:       rhs,
		operation: lhproto.VariableMutationType_EXTEND,
	}
}

func (t *WorkflowThread) RemoveIfPresent(lhs interface{}, rhs interface{}) lhExpression {
	return lhExpression{
		lhs:       lhs,
		rhs:       rhs,
		operation: lhproto.VariableMutationType_REMOVE_IF_PRESENT,
	}
}

func (t *WorkflowThread) RemoveIndex(lhs interface{}, rhs interface{}) lhExpression {
	return lhExpression{
		lhs:       lhs,
		rhs:       rhs,
		operation: lhproto.VariableMutationType_REMOVE_INDEX,
	}
}

func (t *WorkflowThread) RemoveKey(lhs interface{}, key interface{}) lhExpression {
	return lhExpression{
		lhs:       lhs,
		rhs:       key,
		operation: lhproto.VariableMutationType_REMOVE_KEY,
	}
}

func (t *WorkflowThread) WithRetentionPolicy(policy *lhproto.ThreadRetentionPolicy) {
	t.spec.RetentionPolicy = policy
}

func (t *WorkflowThread) AddVariableWithDefault(
	name string, varType lhproto.VariableType, defaultValue interface{},
) *WfRunVariable {
	tempVar := t.addVariable(name, varType)
	tempVar.withDefaultImpl(defaultValue)
	return tempVar
}

func (t *WorkflowThread) Execute(taskDefName interface{}, args ...interface{}) *TaskNodeOutput {
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

func (t *WorkflowThread) DoIf(cond *WorkflowCondition, doIf IfElseBody) *WorkflowIfStatement {
	return t.doIf(cond, doIf)
}

func (s *WorkflowIfStatement) DoElseIf(cond *WorkflowCondition, doIf IfElseBody) *WorkflowIfStatement {
	result := s.thread.doElseIf(*s, cond, doIf)
	return &result
}

func (s *WorkflowIfStatement) DoElse(doElse IfElseBody) {
	if s.wasElseExecuted {
		panic(errors.New("else block has already been executed, cannot add another else block"))
	}
	s.wasElseExecuted = true
	s.thread.doElseIf(*s, nil, doElse)
}

// DoIfElse will be replaced by DoIf and DoElse
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

func (t *WorkflowThread) WaitForThreads(s ...*SpawnedThread) WaitForThreadsNodeOutput {
	return *t.waitForThreads(s...)
}

func (t *WorkflowThread) SpawnThreadForEach(
	arrVar *WfRunVariable, threadName string, threadFunc ThreadFunc, args *map[string]interface{},
) *SpawnedThreads {
	return t.spawnThreadForEach(arrVar, threadName, threadFunc, args)
}

func (t *WorkflowThread) WaitForThreadsList(s *SpawnedThreads) *WaitForThreadsNodeOutput {
	return t.waitForThreadsList(s)
}

func (t *WorkflowThread) AssignUserTask(
	userTaskDefName string, userId, userGroup interface{},
) *UserTaskNodeOutput {
	return t.assignUserTask(userTaskDefName, userId, userGroup)
}

func (t *WorkflowThread) Format(format string, args ...*WfRunVariable) *LHFormatString {
	return t.format(format, args)
}

func (t *WorkflowThread) CancelUserTaskAfter(userTask *UserTaskNodeOutput, delaySeconds interface{}) {
	t.cancelUserTaskAfter(userTask, delaySeconds)
}

func (t *WorkflowThread) CancelUserTaskAfterAssignment(userTask *UserTaskNodeOutput, delaySeconds interface{}) {
	t.cancelUserTaskAfterAssignment(userTask, delaySeconds)
}

func (t *WorkflowThread) ScheduleReminderTask(
	userTask *UserTaskNodeOutput, delaySeconds interface{},
	taskDefName string, args ...interface{},
) {
	t.scheduleReminderTask(userTask, delaySeconds, taskDefName, args)
}

func (t *WorkflowThread) ScheduleReminderTaskOnAssignment(
	userTask *UserTaskNodeOutput, delaySeconds interface{},
	taskDefName string, args ...interface{},
) {
	t.scheduleReminderTaskOnAssignment(userTask, delaySeconds, taskDefName, args)
}

func (t *WorkflowThread) ReleaseToGroupOnDeadline(
	userTask *UserTaskNodeOutput, deadlineSeconds interface{},
) {
	t.releaseToGroupOnDeadline(userTask, deadlineSeconds)
}

func (t *WorkflowThread) ReassignUserTaskOnDeadline(
	userTask *UserTaskNodeOutput, userId, userGroup, deadlineSeconds interface{},
) {
	t.reassignUserTaskOnDeadline(userTask, userId, userGroup, deadlineSeconds)
}

func (t *WorkflowThread) WaitForEvent(eventName string) *ExternalEventNodeOutput {
	return t.waitForEvent(eventName)
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
	nodeOutput NodeOutput,
	specificError *LHErrorType,
	handler ThreadFunc,
) {
	t.handleError(&nodeOutput, specificError, handler)
}

func (t *WorkflowThread) HandleException(
	nodeOutput NodeOutput,
	exceptionName *string,
	handler ThreadFunc,
) {
	t.handleException(&nodeOutput, exceptionName, handler)
}

func (t *WorkflowThread) HandleAnyFailure(
	nodeOutput NodeOutput,
	handler ThreadFunc,
) {
	t.handleAnyFailure(&nodeOutput, handler)
}

func (u *UserTaskNodeOutput) WithNotes(notes interface{}) *UserTaskNodeOutput {
	userTaskNode := u.node.GetUserTask()
	notesVar, err := u.thread.assignVariable(notes)

	if err != nil {
		u.thread.throwError(err)
	}
	userTaskNode.Notes = notesVar
	return u
}

func (u *UserTaskNodeOutput) WithOnCancellationException(exceptionName interface{}) *UserTaskNodeOutput {
	userTaskNode := u.node.GetUserTask()
	onCancellationExceptionName, err := u.thread.assignVariable(exceptionName)

	if err != nil {
		u.thread.throwError(err)
	}
	userTaskNode.OnCancellationExceptionName = onCancellationExceptionName
	return u
}

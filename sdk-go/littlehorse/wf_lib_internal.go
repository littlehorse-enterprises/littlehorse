package littlehorse

import (
	"errors"
	"log"
	"strconv"
	"strings"
	"unicode"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"

	"github.com/ztrue/tracerr"
)

func camelCaseToHostNameCase(s string) string {
	var result strings.Builder
	s = strings.ReplaceAll(s, ".", "-")
	for i, c := range s {
		if unicode.IsUpper(c) {
			if i > 0 {
				result.WriteRune('-')
			}
			result.WriteRune(unicode.ToLower(c))
		} else {
			result.WriteRune(c)
		}
	}
	return result.String()
}

func (l *LHWorkflow) compile() (*lhproto.PutWfSpecRequest, error) {
	seenThreads := make(map[string]ThreadFunc)
	l.funcs = make(map[string]ThreadFunc)

	l.spec.Name = camelCaseToHostNameCase(l.Name)
	l.spec.EntrypointThreadName = l.addSubThread("entrypoint", l.EntrypointThread)
	l.spec.ThreadSpecs = make(map[string]*lhproto.ThreadSpec)

	for {
		curFuncsSize := len(seenThreads)

		for threadName, function := range l.funcs {
			if _, alreadySeen := seenThreads[threadName]; !alreadySeen {
				seenThreads[threadName] = function

				thr := WorkflowThread{
					Name:              threadName,
					isActive:          true,
					wf:                l,
					spec:              lhproto.ThreadSpec{},
					variableMutations: make([]*lhproto.VariableMutation, 0),
				}
				l.threads = append(l.threads, &thr)
				thr.spec.InterruptDefs = make([]*lhproto.InterruptDef, 0)
				thr.spec.VariableDefs = make([]*lhproto.ThreadVarDef, 0)

				// Need to add entrypoint node. We have to do this one manually
				// for now.
				entry := &lhproto.Node{
					Node: &lhproto.Node_Entrypoint{
						Entrypoint: &lhproto.EntrypointNode{},
					},
					OutgoingEdges: make([]*lhproto.Edge, 0),
				}
				nodeName := "0-entrypoint-ENTRYPOINT"
				thr.lastNodeName = &nodeName
				thr.lastNodeCondition = &WorkflowCondition{}
				thr.spec.Nodes = make(map[string]*lhproto.Node)
				thr.spec.Nodes[nodeName] = entry

				// Now do the work for the thread...this calls the user/customer's
				// code, which has calls to thread.ExecuteTask() etc.
				function(&thr)

				// Now add exit node to make a sandwich
				if thr.spec.Nodes[*thr.lastNodeName].GetExit() == nil {
					_, exitNode := thr.createBlankNode("exit", "EXIT")
					exitNode.Node = &lhproto.Node_Exit{
						Exit: &lhproto.ExitNode{},
					}
				}
				thr.isActive = false
				// Now save the thread to the protobuf
				l.spec.ThreadSpecs[threadName] = &thr.spec
			}

		}
		if curFuncsSize == len(l.funcs) {
			break
		}
	}

	return &l.spec, nil
}

func (t *WorkflowThread) createTaskNode(taskDefName interface{}, args []interface{}) *lhproto.TaskNode {

	taskNode := &lhproto.TaskNode{
		Variables: make([]*lhproto.VariableAssignment, 0),
	}

	taskDefNameStr, ok := taskDefName.(string)
	if ok {
		taskNode.TaskToExecute = &lhproto.TaskNode_TaskDefId{
			TaskDefId: &lhproto.TaskDefId{Name: taskDefNameStr},
		}
	} else {
		taskDefVarAssn, err := t.assignVariable(taskDefName)
		if err != nil {
			t.throwError(err)
		}
		taskNode.TaskToExecute = &lhproto.TaskNode_DynamicTask{
			DynamicTask: taskDefVarAssn,
		}
	}

	for _, arg := range args {
		varAssn, err := t.assignVariable(arg)
		if err != nil {
			t.throwError(tracerr.Wrap(err))
		}
		taskNode.Variables = append(taskNode.Variables, varAssn)
	}
	return taskNode
}

func (t *WorkflowThread) executeTask(taskDefName interface{}, args []interface{}) *TaskNodeOutput {
	t.checkIfIsActive()

	// Need to fancily determine the name for the node
	var readableNodeName string
	switch td := taskDefName.(type) {
	case WfRunVariable:
		readableNodeName = td.Name
	case *WfRunVariable:
		readableNodeName = td.Name
	case string:
		readableNodeName = td
	case LHFormatString:
		readableNodeName = td.format
	case *LHFormatString:
		readableNodeName = td.format
	}
	nodeName, node := t.createBlankNode(readableNodeName, "TASK")

	node.Node = &lhproto.Node_Task{
		Task: t.createTaskNode(taskDefName, args),
	}

	taskNodeOutput := TaskNodeOutput{
		Output: NodeOutput{
			nodeName: nodeName,
			thread:   t,
		},
		node:   node,
		parent: t,
	}

	return &taskNodeOutput
}

func (t *WorkflowThread) releaseToGroupOnDeadline(
	userTask *UserTaskOutput, deadlineSeconds interface{},
) {
	t.checkIfIsActive()

	curNode := t.spec.Nodes[*t.lastNodeName]
	if userTask.Output.nodeName != *t.lastNodeName {
		log.Fatal("Trying to edit stale UserTaskOutput!")
	}

	delaySeconds, _ := t.assignVariable(deadlineSeconds)

	originalUserGroup := curNode.GetUserTask().GetUserGroup()
	originalUserId := curNode.GetUserTask().GetUserId()

	// reassignment to a nil userGroup is is allowed if:
	// It's assigned to a User, AND the User has an associated Group.
	if originalUserId == nil {
		t.throwError(tracerr.Wrap(errors.New(
			"need to provide group if reassigning task without userId",
		)))
	}

	if originalUserGroup == nil {
		t.throwError(tracerr.Wrap(errors.New(
			"cannot release to group if group not specified",
		)))
	}
	userGroupAssn := originalUserGroup

	curNode.GetUserTask().Actions = append(curNode.GetUserTask().Actions, &lhproto.UTActionTrigger{
		Hook:         lhproto.UTActionTrigger_ON_TASK_ASSIGNED,
		DelaySeconds: delaySeconds,
		Action: &lhproto.UTActionTrigger_Reassign{
			Reassign: &lhproto.UTActionTrigger_UTAReassign{
				UserGroup: userGroupAssn,
			},
		},
	})
}

func (t *WorkflowThread) reassignUserTaskOnDeadline(
	userTask *UserTaskOutput, userId, userGroup, deadlineSeconds interface{},
) {
	t.checkIfIsActive()

	curNode := t.spec.Nodes[*t.lastNodeName]
	if userTask.Output.nodeName != *t.lastNodeName {
		log.Fatal("Trying to edit stale UserTaskOutput!")
	}

	if userId == nil && userGroup == nil {
		t.throwError(tracerr.New("must provide userId or userGroup"))
	}

	delaySeconds, err := t.assignVariable(deadlineSeconds)
	if err != nil {
		t.throwError(tracerr.Wrap(err))
	}

	var userGroupAssn *lhproto.VariableAssignment
	var userIdAssn *lhproto.VariableAssignment

	if userId != nil {
		userIdAssn, err = t.assignVariable(userId)
		if err != nil {
			t.throwError(tracerr.Wrap(err))
		}
	} else {
		userIdAssn = nil
	}
	if userGroup != nil {
		userGroupAssn, err = t.assignVariable(userGroup)
		if err != nil {
			t.throwError(tracerr.Wrap(err))
		}
	} else {
		userGroupAssn = nil
	}

	curNode.GetUserTask().Actions = append(curNode.GetUserTask().Actions, &lhproto.UTActionTrigger{
		Hook:         lhproto.UTActionTrigger_ON_TASK_ASSIGNED,
		DelaySeconds: delaySeconds,
		Action: &lhproto.UTActionTrigger_Reassign{
			Reassign: &lhproto.UTActionTrigger_UTAReassign{
				UserGroup: userGroupAssn,
				UserId:    userIdAssn,
			},
		},
	})
}

func (t *WorkflowThread) scheduleReminderTask(
	userTask *UserTaskOutput, delaySeconds interface{},
	taskDefName string, args []interface{},
) {
	t.checkIfIsActive()

	delayAssn, err := t.assignVariable(delaySeconds)
	if err != nil {
		log.Fatal(err)
	}

	utaTask := lhproto.UTActionTrigger_Task{
		Task: &lhproto.UTActionTrigger_UTATask{
			Task: t.createTaskNode(taskDefName, args),
		},
	}

	if userTask.Output.nodeName != *(t.lastNodeName) {
		log.Fatal("Tried to edit a stale UserTask node!")
	}

	curNode := t.spec.Nodes[*t.lastNodeName]
	curNode.GetUserTask().Actions = append(curNode.GetUserTask().Actions,
		&lhproto.UTActionTrigger{
			Action:       &utaTask,
			Hook:         lhproto.UTActionTrigger_ON_ARRIVAL,
			DelaySeconds: delayAssn,
		},
	)
}

func (t *WorkflowThread) cancelUserTaskAfter(userTask *UserTaskOutput, delaySeconds interface{}) {
	t.checkIfIsActive()

	delayAssn, err := t.assignVariable(delaySeconds)
	if err != nil {
		log.Fatal(err)
	}

	utaCancel := lhproto.UTActionTrigger_Cancel{
		Cancel: &lhproto.UTActionTrigger_UTACancel{},
	}

	if userTask.Output.nodeName != *(t.lastNodeName) {
		log.Fatal("Tried to edit a stale UserTask node!")
	}
	curNode := t.spec.Nodes[*t.lastNodeName]
	curNode.GetUserTask().Actions = append(curNode.GetUserTask().Actions,
		&lhproto.UTActionTrigger{
			Action:       &utaCancel,
			Hook:         lhproto.UTActionTrigger_ON_ARRIVAL,
			DelaySeconds: delayAssn,
		},
	)
}
func (t *WorkflowThread) cancelUserTaskAfterAssignment(userTask *UserTaskOutput, delaySeconds interface{}) {
	t.checkIfIsActive()

	delayAssn, err := t.assignVariable(delaySeconds)
	if err != nil {
		log.Fatal(err)
	}

	utaCancel := lhproto.UTActionTrigger_Cancel{
		Cancel: &lhproto.UTActionTrigger_UTACancel{},
	}

	if userTask.Output.nodeName != *(t.lastNodeName) {
		log.Fatal("Tried to edit a stale UserTask node!")
	}
	curNode := t.spec.Nodes[*t.lastNodeName]
	curNode.GetUserTask().Actions = append(curNode.GetUserTask().Actions,
		&lhproto.UTActionTrigger{
			Action:       &utaCancel,
			Hook:         lhproto.UTActionTrigger_ON_TASK_ASSIGNED,
			DelaySeconds: delayAssn,
		},
	)
}

func (t *WorkflowThread) scheduleReminderTaskOnAssignment(
	userTask *UserTaskOutput, delaySeconds interface{},
	taskDefName string, args ...interface{},
) {
	t.checkIfIsActive()

	delayAssn, err := t.assignVariable(delaySeconds)
	if err != nil {
		log.Fatal(err)
	}

	utaTask := lhproto.UTActionTrigger_Task{
		Task: &lhproto.UTActionTrigger_UTATask{
			Task: t.createTaskNode(taskDefName, args),
		},
	}

	if userTask.Output.nodeName != *(t.lastNodeName) {
		log.Fatal("Tried to edit a stale UserTask node!")
	}

	curNode := t.spec.Nodes[*t.lastNodeName]
	curNode.GetUserTask().Actions = append(curNode.GetUserTask().Actions,
		&lhproto.UTActionTrigger{
			Action:       &utaTask,
			Hook:         lhproto.UTActionTrigger_ON_TASK_ASSIGNED,
			DelaySeconds: delayAssn,
		},
	)
}

func (t *WorkflowThread) assignUserTask(
	userTaskDefName string, userId, userGroup interface{},
) *UserTaskOutput {
	t.checkIfIsActive()

	utNode := &lhproto.UserTaskNode{
		UserTaskDefName: userTaskDefName,
	}

	if userGroup == nil && userId == nil {
		t.throwError(tracerr.Wrap(errors.New(
			"must specify either userGroup or userId when assigning usertask",
		)))
	}

	if userGroup != nil {
		var err error
		if str, ok := userGroup.(string); ok && strings.TrimSpace(str) == "" {
			t.throwError(tracerr.Wrap(errors.New(
				"userGroup can't be blank when assigning usertask",
			)))
		}
		userGroupAssn, err := t.assignVariable(userGroup)
		if err != nil {
			t.throwError(tracerr.Wrap(err))
		}
		utNode.UserGroup = userGroupAssn
	}

	if userId != nil {
		if str, ok := userId.(string); ok && strings.TrimSpace(str) == "" {
			t.throwError(tracerr.Wrap(errors.New(
				"userId can't be blank when assigning usertask",
			)))
		}
		userIdAssn, err := t.assignVariable(userId)
		if err != nil {
			t.throwError(tracerr.Wrap(err))
		}
		utNode.UserId = userIdAssn
	}

	nodeName, node := t.createBlankNode(userTaskDefName, "USER_TASK")
	node.Node = &lhproto.Node_UserTask{
		UserTask: utNode,
	}

	return &UserTaskOutput{
		thread: t,
		node:   node,
		Output: NodeOutput{
			nodeName: nodeName,
			jsonPath: nil,
			thread:   t,
		},
	}
}

func (t *WorkflowThread) assignVariable(
	val interface{},
) (out *lhproto.VariableAssignment, err error) {
	t.checkIfIsActive()
	switch v := val.(type) {
	case *WfRunVariable:
		out = &lhproto.VariableAssignment{
			JsonPath: v.jsonPath,
			Source: &lhproto.VariableAssignment_VariableName{
				VariableName: v.Name,
			},
		}
	case WfRunVariable:
		out = &lhproto.VariableAssignment{
			JsonPath: v.jsonPath,
			Source: &lhproto.VariableAssignment_VariableName{
				VariableName: v.Name,
			},
		}
	case *LHFormatString:
		formatAssignment, _ := t.assignVariable(v.format)
		var argsAssignments []*lhproto.VariableAssignment = make([]*lhproto.VariableAssignment, 0)

		for _, formatArg := range v.formatArgs {
			argAssignment, err := t.assignVariable(formatArg)
			if err != nil {
				t.throwError(tracerr.Wrap(err))
			}
			argsAssignments = append(argsAssignments, argAssignment)
		}
		out = &lhproto.VariableAssignment{
			Source: &lhproto.VariableAssignment_FormatString_{
				FormatString: &lhproto.VariableAssignment_FormatString{
					Format: formatAssignment,
					Args:   argsAssignments,
				},
			},
		}
	case *NodeOutput:
		out = &lhproto.VariableAssignment{
			JsonPath: v.jsonPath,
			Source: &lhproto.VariableAssignment_NodeOutput{
				NodeOutput: &lhproto.VariableAssignment_NodeOutputReference{
					NodeName: v.nodeName,
				},
			},
		}
	case NodeOutput:
		out = &lhproto.VariableAssignment{
			JsonPath: v.jsonPath,
			Source: &lhproto.VariableAssignment_NodeOutput{
				NodeOutput: &lhproto.VariableAssignment_NodeOutputReference{
					NodeName: v.nodeName,
				},
			},
		}
	case *TaskNodeOutput:
		out = &lhproto.VariableAssignment{
			JsonPath: v.Output.jsonPath,
			Source: &lhproto.VariableAssignment_NodeOutput{
				NodeOutput: &lhproto.VariableAssignment_NodeOutputReference{
					NodeName: v.Output.nodeName,
				},
			},
		}
	case TaskNodeOutput:
		out = &lhproto.VariableAssignment{
			JsonPath: v.Output.jsonPath,
			Source: &lhproto.VariableAssignment_NodeOutput{
				NodeOutput: &lhproto.VariableAssignment_NodeOutputReference{
					NodeName: v.Output.nodeName,
				},
			},
		}
	case *LHExpression:
		lhs, lhsErr := t.assignVariable(v.lhs)

		if lhsErr != nil {
			t.throwError(lhsErr)
		}

		rhs, rhsErr := t.assignVariable(v.rhs)

		if rhsErr != nil {
			t.throwError(rhsErr)
		}

		out = &lhproto.VariableAssignment{
			JsonPath: nil,
			Source: &lhproto.VariableAssignment_Expression_{
				Expression: &lhproto.VariableAssignment_Expression{
					Lhs:       lhs,
					Operation: v.operation,
					Rhs:       rhs,
				},
			},
		}
	case LHExpression:
		lhs, lhsErr := t.assignVariable(v.lhs)

		if lhsErr != nil {
			t.throwError(lhsErr)
		}

		rhs, rhsErr := t.assignVariable(v.rhs)

		if rhsErr != nil {
			t.throwError(rhsErr)
		}

		out = &lhproto.VariableAssignment{
			JsonPath: nil,
			Source: &lhproto.VariableAssignment_Expression_{
				Expression: &lhproto.VariableAssignment_Expression{
					Lhs:       lhs,
					Operation: v.operation,
					Rhs:       rhs,
				},
			},
		}
	default:
		var tmp *lhproto.VariableValue
		tmp, err = InterfaceToVarVal(v)
		if tmp != nil {
			out = &lhproto.VariableAssignment{
				Source: &lhproto.VariableAssignment_LiteralValue{
					LiteralValue: tmp,
				},
			}
		}
	}

	return out, err
}

func (t *WorkflowThread) createBlankNode(name, nType string) (string, *lhproto.Node) {
	t.checkIfIsActive()
	nodeName := t.getNodeName(name, nType)

	// This should only be called after the 'ENTRYPOINT' node has already been created
	if t.lastNodeName == nil {
		panic("Not possible to have lastNodeName nil at this point")
	}

	// Need to add an edge from that node to this node
	lastNode := t.spec.Nodes[*t.lastNodeName]

	edge := lhproto.Edge{
		SinkNodeName:      nodeName,
		VariableMutations: t.collectVariableMutations(),
	}

	if t.lastNodeCondition != nil {
		edge.Condition = t.lastNodeCondition.spec
		t.lastNodeCondition = nil
	}
	lastNode.OutgoingEdges = append(lastNode.OutgoingEdges, &edge)

	node := &lhproto.Node{
		OutgoingEdges:   make([]*lhproto.Edge, 0),
		FailureHandlers: make([]*lhproto.FailureHandlerDef, 0),
	}

	t.spec.Nodes[nodeName] = node
	t.lastNodeName = &nodeName
	return nodeName, node
}

func (t *WorkflowThread) getNodeName(humanName, nodeType string) string {
	t.checkIfIsActive()
	return strconv.Itoa(len(t.spec.Nodes)) + "-" + humanName + "-" + nodeType
}

func (w *LHWorkflow) addSubThread(threadName string, tf ThreadFunc) string {
	// Note: no need to convert thread name to hostNameCase. It is not a getable.
	w.funcs[threadName] = tf
	return threadName
}

func (w *WfRunVariable) withDefaultImpl(defaultValue interface{}) *WfRunVariable {
	if defaultValue != nil {
		defaultVarVal, err := InterfaceToVarVal(defaultValue)
		if err != nil {
			log.Fatal(err)
		}
		if *GetVarType(defaultVarVal) != w.threadVarDef.VarDef.TypeDef.Type {
			log.Fatal("provided default value for variable " + w.Name + " didn't match type " + w.threadVarDef.VarDef.TypeDef.Type.String())
		}
		w.threadVarDef.VarDef.DefaultValue = defaultVarVal
	}

	return w
}

func (w *WfRunVariable) searchableImpl() *WfRunVariable {
	w.threadVarDef.Searchable = true
	return w
}

func (w *WfRunVariable) requiredImpl() *WfRunVariable {
	w.threadVarDef.Required = true
	return w
}

// TODO: Add validation that fieldPath is properly set.
func (w *WfRunVariable) searchableOnImpl(fieldPath string, fieldType lhproto.VariableType) *WfRunVariable {
	w.threadVarDef.JsonIndexes = append(w.threadVarDef.JsonIndexes, &lhproto.JsonIndex{
		FieldPath: fieldPath,
		FieldType: fieldType,
	})
	return w
}

func (w *WfRunVariable) maskedValueImpl() *WfRunVariable {
	w.threadVarDef.VarDef.TypeDef.Masked = true
	return w
}

func (w *WfRunVariable) withAccessLevel(accessLevel lhproto.WfRunVariableAccessLevel) WfRunVariable {
	w.threadVarDef.AccessLevel = accessLevel
	return *w
}

func (w *WfRunVariable) jsonPathImpl(path string) WfRunVariable {
	if w.jsonPath != nil {
		w.thread.throwError(
			errors.New("Variable " + w.Name + " was jsonpath'ed twice!"),
		)
	}
	if w.VarType != nil && *w.VarType != lhproto.VariableType_JSON_ARR && *w.VarType != lhproto.VariableType_JSON_OBJ {
		w.thread.throwError(errors.New(
			"Cannot jsonpath on var of type " + w.VarType.String(),
		))
	}
	return WfRunVariable{
		Name:     w.Name,
		thread:   w.thread,
		VarType:  nil,
		jsonPath: &path,
	}
}

func (n *NodeOutput) jsonPathImpl(path string) NodeOutput {
	if n.jsonPath != nil {
		n.thread.throwError(
			errors.New("node output jsonpathed twice"),
		)
	}
	return NodeOutput{
		nodeName: n.nodeName,
		thread:   n.thread,
		jsonPath: &path,
	}
}

func (n *NodeOutput) handleExceptionOnChild(handler ThreadFunc, exceptionName *string) {
	n.thread.checkIfIsActive()
	node := n.thread.spec.Nodes[n.nodeName]
	if node.GetWaitForThreads() == nil {
		n.thread.throwError(errors.New("can only call handleExceptionOnChild on WaitForThreads Node"))
	}

	var threadName string
	if exceptionName != nil {
		threadName = "exn-handler-" + n.nodeName + "-" + *exceptionName
	} else {
		threadName = "exn-handler-" + n.nodeName
	}
	threadName = n.thread.wf.addSubThread(threadName, handler)

	var failureHandler *lhproto.FailureHandlerDef
	if exceptionName != nil {
		failureHandler = &lhproto.FailureHandlerDef{
			HandlerSpecName: threadName,
			FailureToCatch: &lhproto.FailureHandlerDef_SpecificFailure{
				SpecificFailure: *exceptionName,
			},
		}
	} else {
		failureHandler = &lhproto.FailureHandlerDef{
			HandlerSpecName: threadName,
			FailureToCatch: &lhproto.FailureHandlerDef_AnyFailureOfType{
				AnyFailureOfType: lhproto.FailureHandlerDef_FAILURE_TYPE_EXCEPTION,
			},
		}
	}
	node.GetWaitForThreads().PerThreadFailureHandlers = append(
		node.GetWaitForThreads().PerThreadFailureHandlers, failureHandler)
	PrintProto(node)
}

func (n *NodeOutput) handleErrorOnChild(handler ThreadFunc, errorName *string) {
	n.thread.checkIfIsActive()
	node := n.thread.spec.Nodes[n.nodeName]
	if node.GetWaitForThreads() == nil {
		n.thread.throwError(errors.New("can only call handleErrorOnChild on WaitForThreads Node"))
	}

	var threadName string
	if errorName != nil {
		threadName = "error-handler-" + n.nodeName + "-" + *errorName
	} else {
		threadName = "error-handler-" + n.nodeName
	}
	threadName = n.thread.wf.addSubThread(threadName, handler)

	var failureHandler *lhproto.FailureHandlerDef
	if errorName != nil {
		failureHandler = &lhproto.FailureHandlerDef{
			HandlerSpecName: threadName,
			FailureToCatch: &lhproto.FailureHandlerDef_SpecificFailure{
				SpecificFailure: *errorName,
			},
		}
	} else {
		failureHandler = &lhproto.FailureHandlerDef{
			HandlerSpecName: threadName,
			FailureToCatch: &lhproto.FailureHandlerDef_AnyFailureOfType{
				AnyFailureOfType: lhproto.FailureHandlerDef_FAILURE_TYPE_ERROR,
			},
		}
	}
	node.GetWaitForThreads().PerThreadFailureHandlers = append(
		node.GetWaitForThreads().PerThreadFailureHandlers, failureHandler)
}

func (n *NodeOutput) handleAnyFailureOnChild(handler ThreadFunc) {
	n.thread.checkIfIsActive()
	node := n.thread.spec.Nodes[n.nodeName]
	if node.GetWaitForThreads() == nil {
		n.thread.throwError(errors.New("can only call handleErrorOnChild on WaitForThreads Node"))
	}

	threadName := "failure-handler-" + n.nodeName + "-ANY_FAILURE"
	threadName = n.thread.wf.addSubThread(threadName, handler)

	failureHandler := &lhproto.FailureHandlerDef{
		HandlerSpecName: threadName,
	}
	node.GetWaitForThreads().PerThreadFailureHandlers = append(
		node.GetWaitForThreads().PerThreadFailureHandlers, failureHandler)
}

func (t *WorkflowThread) throwError(e error) {
	// For now, we just panic, since it provides a way to get a stacktrace.
	// In the future, we'll do more clean things and try to find out how to
	// bubble up an error with relevant info so that the user can catch it and do
	// stuff with it. But for now, for a demo, panic'ing is fine.
	panic(e)
}

func (t *WorkflowThread) mutate(
	lhs *WfRunVariable,
	mType lhproto.VariableMutationType,
	rhs interface{},
) {
	t.checkIfIsActive()
	mutation := &lhproto.VariableMutation{
		LhsName:     lhs.Name,
		LhsJsonPath: lhs.jsonPath,
		Operation:   mType,
	}

	rhsValue, err := t.assignVariable(rhs)

	if err != nil {
		t.throwError(errors.New("can't assign RHS value"))
	}

	mutation.RhsValue = &lhproto.VariableMutation_RhsAssignment{
		RhsAssignment: rhsValue,
	}

	t.variableMutations = append(t.variableMutations, mutation)
}
func (tn *TaskNodeOutput) withExponentialBackoffImpl(policy *lhproto.ExponentialBackoffRetryPolicy) *TaskNodeOutput {
	tn.parent.overrideTaskExponentialBackoffPolicy(tn, policy)
	return tn
}

func (tn *TaskNodeOutput) withRetriesImpl(retries int32) *TaskNodeOutput {
	tn.parent.overrideTaskRetries(tn, retries)
	return tn
}

func (t *WorkflowThread) addVariable(
	name string, varType lhproto.VariableType,
) *WfRunVariable {
	t.checkIfIsActive()
	varDef := &lhproto.VariableDef{
		TypeDef: &lhproto.TypeDefinition{Type: varType},
		Name:    name,
	}

	threadVarDef := &lhproto.ThreadVarDef{
		VarDef:      varDef,
		AccessLevel: lhproto.WfRunVariableAccessLevel_PRIVATE_VAR,
	}

	t.spec.VariableDefs = append(t.spec.VariableDefs, threadVarDef)

	return &WfRunVariable{
		Name:         name,
		VarType:      &varType,
		thread:       t,
		threadVarDef: threadVarDef,
	}
}

func (t *WorkflowThread) condition(
	lhs interface{}, op lhproto.Comparator, rhs interface{},
) *WorkflowCondition {
	t.checkIfIsActive()
	cond := &lhproto.EdgeCondition{
		Comparator: op,
	}

	var err error

	cond.Left, err = t.assignVariable(lhs)

	if err != nil {
		t.throwError(tracerr.Wrap(err))
	}

	cond.Right, err = t.assignVariable(rhs)
	if err != nil {
		t.throwError(tracerr.Wrap(err))
	}

	return &WorkflowCondition{
		spec: cond,
	}
}

func (t *WorkflowThread) addNopNode() {
	t.checkIfIsActive()
	_, n := t.createBlankNode("nop", "NOP")
	n.Node = &lhproto.Node_Nop{
		Nop: &lhproto.NopNode{},
	}
}

func (t *WorkflowThread) doIf(cond *WorkflowCondition, doIf IfElseBody) *WorkflowIfStatement {
	t.checkIfIsActive()
	// The tree looks like:
	/* T
	   |\
	   A |
	   |/
	   B
	*/
	// In that ASCII picture:
	// T is top of the tree
	// A is the if body
	// B is the bottom of the tree

	// Top of the tree. This creates the T node
	t.addNopNode()
	firstNopNodeName := t.lastNodeName
	firstNode := t.spec.Nodes[*firstNopNodeName]
	t.lastNodeCondition = cond

	// Do the work. This adds the 'A'
	doIf(t)

	// Close off the tree. This creates the B node
	t.addNopNode()
	lastNodeName := t.lastNodeName

	firstNode.OutgoingEdges = append(
		firstNode.OutgoingEdges,
		&lhproto.Edge{
			SinkNodeName: *lastNodeName,
		},
	)

	return &WorkflowIfStatement{firstNopNodeName: *firstNopNodeName,
		lastNopNodeName: *lastNodeName,
		wasElseExecuted: false,
		thread:          t}
}

func (t *WorkflowThread) doElseIf(ifStatement WorkflowIfStatement, cond *WorkflowCondition, doElseIf IfElseBody) WorkflowIfStatement {
	firstNopNode := t.spec.Nodes[ifStatement.firstNopNodeName]
	elseEdge := firstNopNode.OutgoingEdges[len(firstNopNode.OutgoingEdges)-1]
	// Remove else edge from the first NOP node
	firstNopNode.OutgoingEdges = removeEdge(firstNopNode.OutgoingEdges, elseEdge)
	lastNodeOfParentThread := t.spec.Nodes[*t.lastNodeName]
	lastNodeNameOfParentThread := t.lastNodeName

	doElseIf(t)

	// Get the last node of the Else If body to reference later
	lastNodeOfBody := t.spec.Nodes[*t.lastNodeName]

	// If no nodes were added from body
	if lastNodeOfParentThread == lastNodeOfBody {
		// Add edge from nop 1 to nop 2 with variable mutations
		firstNopNode.OutgoingEdges = append(
			firstNopNode.OutgoingEdges,
			t.buildNewEdge(ifStatement.lastNopNodeName, cond, t.collectVariableMutations()),
		)
	} else {
		lastOutgoingEdge := lastNodeOfParentThread.OutgoingEdges[len(lastNodeOfParentThread.OutgoingEdges)-1]
		// Remove edge between last node of parent thread and first node of body
		lastNodeOfParentThread.OutgoingEdges = removeEdge(lastNodeOfParentThread.OutgoingEdges, lastOutgoingEdge)
		// Get the first node of the body
		firstNodeOfBodyName := lastOutgoingEdge.SinkNodeName

		// Add an edge from the first NOP node to the first node of the body
		firstNopNode.OutgoingEdges = append(
			firstNopNode.OutgoingEdges,
			t.buildNewEdge(firstNodeOfBodyName, cond, lastOutgoingEdge.VariableMutations),
		)

		// Add an edge from the last node of the body to the last NOP node
		lastNodeOfBody.OutgoingEdges = append(
			lastNodeOfBody.OutgoingEdges,
			&lhproto.Edge{
				SinkNodeName:      ifStatement.lastNopNodeName,
				VariableMutations: t.collectVariableMutations(),
			},
		)
	}

	// If else condition was not replaced, add it back
	if cond != nil {
		firstNopNode.OutgoingEdges = append(firstNopNode.OutgoingEdges, elseEdge)
	}

	t.lastNodeName = lastNodeNameOfParentThread

	return WorkflowIfStatement{firstNopNodeName: ifStatement.firstNopNodeName,
		lastNopNodeName: ifStatement.lastNopNodeName,
		wasElseExecuted: false,
		thread:          t}
}

func removeEdge(edges []*lhproto.Edge, edgeToRemove *lhproto.Edge) []*lhproto.Edge {
	for i, edge := range edges {
		if edge == edgeToRemove {
			// Remove the element by slicing around it
			result := append(edges[:i], edges[i+1:]...)
			return result
		}
	}
	return edges // Return original edges if the edge was not found
}

func (t *WorkflowThread) buildNewEdge(sinkNodeName string, cond *WorkflowCondition, variableMutations []*lhproto.VariableMutation) *lhproto.Edge {
	if cond != nil {
		return &lhproto.Edge{
			SinkNodeName:      sinkNodeName,
			VariableMutations: variableMutations,
			Condition:         cond.spec,
		}
	}

	return &lhproto.Edge{
		SinkNodeName:      sinkNodeName,
		VariableMutations: variableMutations,
	}
}

func (t *WorkflowThread) doIfElse(
	cond *WorkflowCondition, doIf IfElseBody, doElse IfElseBody,
) {
	t.checkIfIsActive()

	t.addNopNode()
	treeRootNodeName := t.lastNodeName
	t.lastNodeCondition = cond
	doIf(t)

	lastConditionFromIfBlock := t.lastNodeCondition
	lastNodeFromIfBlockName := t.lastNodeName
	variablesFromIfBlock := t.collectVariableMutations()

	// Go back to the tree root
	t.lastNodeName = treeRootNodeName
	t.lastNodeCondition = &WorkflowCondition{spec: cond.getReverse()}
	doElse(t)

	t.addNopNode()

	ifBlockEdge := lhproto.Edge{
		SinkNodeName:      *t.lastNodeName,
		VariableMutations: variablesFromIfBlock,
	}

	// If the treeRootNodeName is equal to the lastNodeFromIfBlockName it means that
	// no node was created within the if block, thus the edge of the starting NOP should be created
	// with the appropriate conditional
	if lastNodeFromIfBlockName == treeRootNodeName {
		ifBlockEdge.Condition = lastConditionFromIfBlock.spec
	}

	t.spec.Nodes[*lastNodeFromIfBlockName].OutgoingEdges = append(
		t.spec.Nodes[*lastNodeFromIfBlockName].OutgoingEdges,
		&ifBlockEdge,
	)
}

func (t *WorkflowThread) collectVariableMutations() []*lhproto.VariableMutation {
	variablesFromIfBlock := make([]*lhproto.VariableMutation, len(t.variableMutations))
	copy(variablesFromIfBlock, t.variableMutations)
	t.variableMutations = nil
	return variablesFromIfBlock
}

func (t *WorkflowThread) doWhile(cond *WorkflowCondition, whileBody ThreadFunc) {
	t.checkIfIsActive()
	// The tree looks like:
	/* T
	 /|\
	| A |
	 \|/
	  B
	*/
	// In that ASCII picture:
	// T is top of the tree
	// A is the while body
	// B is the bottom of the tree and can go back to the T

	// Top of the tree. This creates the T node
	t.addNopNode()
	topOfTreeNode := t.spec.Nodes[*t.lastNodeName]
	topOfTreeNodeName := t.lastNodeName

	// Do the work. This adds the 'A'
	t.lastNodeCondition = cond
	whileBody(t)

	// Close off the tree. This creates the B node
	t.addNopNode()
	bottomOfTreeNode := t.spec.Nodes[*t.lastNodeName]
	bottomOfTreeNodeName := t.lastNodeName

	// Now add the sideways path from T directly to B
	topOfTreeNode.OutgoingEdges = append(
		topOfTreeNode.OutgoingEdges,
		&lhproto.Edge{
			SinkNodeName: *bottomOfTreeNodeName,
			Condition:    cond.getReverse(),
		},
	)

	// Now add the sideways path from B directly to T
	bottomOfTreeNode.OutgoingEdges = append(
		bottomOfTreeNode.OutgoingEdges,
		&lhproto.Edge{
			SinkNodeName: *topOfTreeNodeName,
			Condition:    cond.spec,
		},
	)
}

func (c *WorkflowCondition) getReverse() *lhproto.EdgeCondition {
	out := &lhproto.EdgeCondition{}
	out.Left = c.spec.Left
	out.Right = c.spec.Right
	switch c.spec.Comparator {
	case lhproto.Comparator_LESS_THAN:
		out.Comparator = lhproto.Comparator_GREATER_THAN_EQ
	case lhproto.Comparator_GREATER_THAN:
		out.Comparator = lhproto.Comparator_LESS_THAN_EQ
	case lhproto.Comparator_GREATER_THAN_EQ:
		out.Comparator = lhproto.Comparator_LESS_THAN
	case lhproto.Comparator_LESS_THAN_EQ:
		out.Comparator = lhproto.Comparator_GREATER_THAN
	case lhproto.Comparator_EQUALS:
		out.Comparator = lhproto.Comparator_NOT_EQUALS
	case lhproto.Comparator_NOT_EQUALS:
		out.Comparator = lhproto.Comparator_EQUALS
	case lhproto.Comparator_IN:
		out.Comparator = lhproto.Comparator_NOT_IN
	case lhproto.Comparator_NOT_IN:
		out.Comparator = lhproto.Comparator_IN
	}

	return out
}

func (t *WorkflowThread) overrideTaskRetries(taskNodeOutput *TaskNodeOutput, retries int32) {
	t.checkIfIsActive()

	node := t.spec.Nodes[taskNodeOutput.Output.nodeName]
	if node.GetTask() == nil {
		t.throwError(errors.New("impossible to not have task node here"))
	}

	node.GetTask().Retries = retries
}

func (t *WorkflowThread) overrideTaskExponentialBackoffPolicy(taskNodeOutput *TaskNodeOutput, policy *lhproto.ExponentialBackoffRetryPolicy) {
	t.checkIfIsActive()

	node := t.spec.Nodes[taskNodeOutput.Output.nodeName]
	if node.GetTask() == nil {
		t.throwError(errors.New("impossible to not have task node here"))
	}

	node.GetTask().ExponentialBackoff = policy
}

func (t *WorkflowThread) addTimeoutToExtEvtNode(extEvnodeOutput *ExternalEventNodeOutput, timeoutSeconds int64) {
	t.checkIfIsActive()

	node := t.spec.Nodes[extEvnodeOutput.Output.nodeName]
	node.GetExternalEvent().TimeoutSeconds = &lhproto.VariableAssignment{
		JsonPath: nil,
		Source: &lhproto.VariableAssignment_LiteralValue{
			LiteralValue: &lhproto.VariableValue{
				Value: &lhproto.VariableValue_Int{
					Int: int64(timeoutSeconds),
				},
			},
		},
	}
}

func (t *WorkflowThread) addTimeoutToTaskNode(taskNodeOutput *TaskNodeOutput, timeoutSeconds int64) {
	t.checkIfIsActive()

	node := t.spec.Nodes[taskNodeOutput.Output.nodeName]

	node.GetTask().TimeoutSeconds = int32(timeoutSeconds)
}

func (t *WorkflowThread) spawnThread(
	tFunc ThreadFunc, threadName string, args map[string]interface{},
) *SpawnedThread {
	t.checkIfIsActive()
	threadName = t.wf.addSubThread(threadName, tFunc)

	nodeName, node := t.createBlankNode(threadName, "SPAWN_THREAD")
	cachedThreadVar := t.addVariable(nodeName, lhproto.VariableType_INT)

	node.Node = &lhproto.Node_StartThread{
		StartThread: &lhproto.StartThreadNode{
			ThreadSpecName: threadName,
			Variables:      make(map[string]*lhproto.VariableAssignment),
		},
	}

	for argName, arg := range args {
		varAssn, err := t.assignVariable(arg)
		if err != nil {
			t.throwError(tracerr.Wrap(err))
		}
		node.GetStartThread().Variables[argName] = varAssn
	}

	t.mutate(
		cachedThreadVar,
		lhproto.VariableMutationType_ASSIGN,
		NodeOutput{
			nodeName, nil, t,
		},
	)

	return &SpawnedThread{
		thread:       t,
		threadNumVar: cachedThreadVar,
	}
}

func (t *WorkflowThread) waitForThreads(s ...*SpawnedThread) *NodeOutput {
	t.checkIfIsActive()
	nodeName, node := t.createBlankNode("threads", "WAIT_FOR_THREADS")
	node.Node = &lhproto.Node_WaitForThreads{
		WaitForThreads: &lhproto.WaitForThreadsNode{
			ThreadsToWaitFor: &lhproto.WaitForThreadsNode_Threads{
				Threads: &lhproto.WaitForThreadsNode_ThreadsToWaitFor{
					Threads: make([]*lhproto.WaitForThreadsNode_ThreadToWaitFor, 0),
				},
			},
		},
	}

	for _, spawnedThread := range s {
		threadRunNumberAssn, _ := t.assignVariable(spawnedThread.threadNumVar)

		node.GetWaitForThreads().GetThreads().Threads = append(node.GetWaitForThreads().GetThreads().Threads,
			&lhproto.WaitForThreadsNode_ThreadToWaitFor{
				ThreadRunNumber: threadRunNumberAssn,
			},
		)
	}

	return &NodeOutput{
		nodeName: nodeName,
		jsonPath: nil,
		thread:   t,
	}
}

func (t *WorkflowThread) spawnThreadForEach(
	arrVar *WfRunVariable, threadName string, threadFunc ThreadFunc, args *map[string]interface{},
) *SpawnedThreads {
	t.checkIfIsActive()

	if *arrVar.VarType != lhproto.VariableType_JSON_ARR {
		t.throwError(tracerr.Wrap(errors.New("can only iterate over JSON_ARR variable")))
	}

	finalThreadName := t.wf.addSubThread(threadName, threadFunc)
	iterableAssn, err := t.assignVariable(arrVar)
	if err != nil {
		t.throwError(tracerr.Wrap(err))
	}

	subNode := &lhproto.StartMultipleThreadsNode{
		ThreadSpecName: finalThreadName,
		Iterable:       iterableAssn,
		Variables:      make(map[string]*lhproto.VariableAssignment),
	}

	if args != nil {
		for name, arg := range *args {
			varAssn, err := t.assignVariable(arg)
			if err != nil {
				t.throwError(tracerr.Wrap(err))
			}
			subNode.Variables[name] = varAssn
		}
	}

	nodeName, node := t.createBlankNode(threadName, "START_MULTIPLE_THREADS")
	node.Node = &lhproto.Node_StartMultipleThreads{
		StartMultipleThreads: subNode,
	}

	internalThreadNumbersVar := t.addVariable(
		nodeName, lhproto.VariableType_JSON_ARR,
	)

	t.mutate(
		internalThreadNumbersVar,
		lhproto.VariableMutationType_ASSIGN,
		NodeOutput{nodeName: nodeName, thread: t},
	)

	return &SpawnedThreads{
		thread:     t,
		threadsVar: internalThreadNumbersVar,
	}
}

func (t *WorkflowThread) waitForThreadsList(s *SpawnedThreads) NodeOutput {
	t.checkIfIsActive()
	threadListAssn, err := t.assignVariable(s.threadsVar)
	if err != nil {
		t.throwError(tracerr.Wrap(err))
	}

	subNode := &lhproto.WaitForThreadsNode{
		ThreadsToWaitFor: &lhproto.WaitForThreadsNode_ThreadList{
			ThreadList: threadListAssn,
		},
	}

	nodeName, node := t.createBlankNode("threads", "WAIT_FOR_THREADS")
	node.Node = &lhproto.Node_WaitForThreads{
		WaitForThreads: subNode,
	}
	return NodeOutput{
		thread:   t,
		nodeName: nodeName,
	}
}

func (t *WorkflowThread) waitForEvent(eventName string) *NodeOutput {
	t.checkIfIsActive()
	nodeName, node := t.createBlankNode(eventName, "EXTERNAL_EVENT")

	node.Node = &lhproto.Node_ExternalEvent{
		ExternalEvent: &lhproto.ExternalEventNode{
			ExternalEventDefId: &lhproto.ExternalEventDefId{Name: eventName},
		},
	}

	return &NodeOutput{
		nodeName: nodeName,
		jsonPath: nil,
		thread:   t,
	}
}

func (t *WorkflowThread) throwEvent(workflowEventDefName string, content interface{}) {
	t.checkIfIsActive()
	_, node := t.createBlankNode("throw-"+workflowEventDefName, "THROW_EVENT")

	contentAssn, err := t.assignVariable(content)
	if err != nil {
		t.throwError(tracerr.Wrap(err))
	}
	node.Node = &lhproto.Node_ThrowEvent{
		ThrowEvent: &lhproto.ThrowEventNode{
			EventDefId: &lhproto.WorkflowEventDefId{
				Name: workflowEventDefName,
			},
			Content: contentAssn,
		},
	}
}

func (t *WorkflowThread) format(format string, args []*WfRunVariable) *LHFormatString {
	return &LHFormatString{
		format:     format,
		thread:     t,
		formatArgs: args,
	}
}

func (t *WorkflowThread) fail(content interface{}, failureName string, msg *string) {
	t.checkIfIsActive()
	_, node := t.createBlankNode(failureName, "EXIT")

	contentVarVal, err := t.assignVariable(content)
	if err != nil {
		t.throwError(tracerr.Wrap(err))
	}
	var message string
	if msg == nil {
		message = ""
	} else {
		message = *msg
	}

	node.Node = &lhproto.Node_Exit{
		Exit: &lhproto.ExitNode{
			FailureDef: &lhproto.FailureDef{
				FailureName: failureName,
				Content:     contentVarVal,
				Message:     message,
			},
		},
	}
}

func (t *WorkflowThread) sleep(sleepSeconds int) {
	t.checkIfIsActive()
	_, node := t.createBlankNode("sleep", "SLEEP")

	sleepSeconds64 := int64(sleepSeconds)

	sleepNode := &lhproto.Node_Sleep{
		Sleep: &lhproto.SleepNode{
			SleepLength: &lhproto.SleepNode_RawSeconds{
				RawSeconds: &lhproto.VariableAssignment{
					Source: &lhproto.VariableAssignment_LiteralValue{
						LiteralValue: &lhproto.VariableValue{
							Value: &lhproto.VariableValue_Int{Int: sleepSeconds64},
						},
					},
				},
			},
		},
	}

	node.Node = sleepNode
}

func (t *WorkflowThread) handleInterrupt(interruptName string, handler ThreadFunc) {
	t.checkIfIsActive()
	handlerName := t.wf.addSubThread("interrupt-"+interruptName, handler)
	t.spec.InterruptDefs = append(t.spec.InterruptDefs, &lhproto.InterruptDef{
		ExternalEventDefId: &lhproto.ExternalEventDefId{Name: interruptName},
		HandlerSpecName:    handlerName,
	})
}

func (t *WorkflowThread) handleError(
	nodeOutput *NodeOutput,
	specificError *LHErrorType,
	handler ThreadFunc,
) {
	t.checkIfIsActive()
	node := t.spec.Nodes[nodeOutput.nodeName]

	var fhd *lhproto.FailureHandlerDef

	if specificError != nil {
		failureName := string(*specificError)
		handlerName := "error-handler-" + failureName + "-" + nodeOutput.nodeName
		threadName := t.wf.addSubThread(handlerName, handler)

		fhd = &lhproto.FailureHandlerDef{
			FailureToCatch: &lhproto.FailureHandlerDef_SpecificFailure{
				SpecificFailure: failureName,
			},
			HandlerSpecName: threadName,
		}
	} else {
		handlerName := "error-handler-" + nodeOutput.nodeName
		threadName := t.wf.addSubThread(handlerName, handler)

		fhd = &lhproto.FailureHandlerDef{
			FailureToCatch: &lhproto.FailureHandlerDef_AnyFailureOfType{
				AnyFailureOfType: lhproto.FailureHandlerDef_FAILURE_TYPE_ERROR,
			},
			HandlerSpecName: threadName,
		}
	}

	node.FailureHandlers = append(node.FailureHandlers, fhd)
}

func (t *WorkflowThread) handleAnyFailure(
	nodeOutput *NodeOutput, handler ThreadFunc,
) {
	t.checkIfIsActive()
	node := t.spec.Nodes[nodeOutput.nodeName]
	handlerName := "exception-handler-all-" + nodeOutput.nodeName
	threadName := t.wf.addSubThread(handlerName, handler)

	node.FailureHandlers = append(node.FailureHandlers, &lhproto.FailureHandlerDef{
		FailureToCatch:  nil, // catches all Failures
		HandlerSpecName: threadName,
	})
}

func (t *WorkflowThread) handleException(
	nodeOutput *NodeOutput,
	exceptionName *string,
	handler ThreadFunc,
) {
	t.checkIfIsActive()
	node := t.spec.Nodes[nodeOutput.nodeName]

	var fhd *lhproto.FailureHandlerDef

	if exceptionName != nil {
		handlerName := "exn-handler-" + *exceptionName + "-" + nodeOutput.nodeName
		threadName := t.wf.addSubThread(handlerName, handler)

		fhd = &lhproto.FailureHandlerDef{
			FailureToCatch: &lhproto.FailureHandlerDef_SpecificFailure{
				SpecificFailure: *exceptionName,
			},
			HandlerSpecName: threadName,
		}
	} else {
		handlerName := "exn-handler-" + nodeOutput.nodeName
		threadName := t.wf.addSubThread(handlerName, handler)

		fhd = &lhproto.FailureHandlerDef{
			FailureToCatch: &lhproto.FailureHandlerDef_AnyFailureOfType{
				AnyFailureOfType: lhproto.FailureHandlerDef_FAILURE_TYPE_EXCEPTION,
			},
			HandlerSpecName: threadName,
		}
	}

	node.FailureHandlers = append(node.FailureHandlers, fhd)
}

func (t *WorkflowThread) checkIfIsActive() {
	if !t.isActive {
		t.throwError(tracerr.Wrap(errors.New("using a inactive thread")))
	}
}

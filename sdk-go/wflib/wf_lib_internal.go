package wflib

import (
	"errors"
	"log"
	"strconv"
	"strings"
	"unicode"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common/model"
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

func (l *LHWorkflow) compile() (*model.PutWfSpecRequest, error) {
	seenThreads := make(map[string]ThreadFunc)
	l.funcs = make(map[string]ThreadFunc)

	l.spec.Name = camelCaseToHostNameCase(l.Name)
	l.spec.EntrypointThreadName = l.addSubThread("entrypoint", l.EntrypointThread)
	l.spec.ThreadSpecs = make(map[string]*model.ThreadSpec)

	for {
		curFuncsSize := len(seenThreads)

		for threadName, function := range l.funcs {
			if _, alreadySeen := seenThreads[threadName]; !alreadySeen {
				seenThreads[threadName] = function

				thr := WorkflowThread{
					Name:              threadName,
					isActive:          true,
					wf:                l,
					spec:              model.ThreadSpec{},
					variableMutations: make([]*model.VariableMutation, 0),
				}
				thr.spec.InterruptDefs = make([]*model.InterruptDef, 0)
				thr.spec.VariableDefs = make([]*model.ThreadVarDef, 0)

				// Need to add entrypoint node. We have to do this one manually
				// for now.
				entry := &model.Node{
					Node: &model.Node_Entrypoint{
						Entrypoint: &model.EntrypointNode{},
					},
					OutgoingEdges: make([]*model.Edge, 0),
				}
				nodeName := "0-ENTRYPOINT"
				thr.lastNodeName = &nodeName
				thr.lastNodeCondition = &WorkflowCondition{}
				thr.spec.Nodes = make(map[string]*model.Node)
				thr.spec.Nodes[nodeName] = entry

				// Now do the work for the thread...this calls the user/customer's
				// code, which has calls to thread.ExecuteTask() etc.
				function(&thr)

				// Now add exit node to make a sandwich
				_, exitNode := thr.createBlankNode("exit", "EXIT")
				exitNode.Node = &model.Node_Exit{
					Exit: &model.ExitNode{},
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

func (t *WorkflowThread) createTaskNode(taskDefName interface{}, args []interface{}) *model.TaskNode {

	taskNode := &model.TaskNode{
		Variables: make([]*model.VariableAssignment, 0),
	}

	taskDefNameStr, ok := taskDefName.(string)
	if ok {
		taskNode.TaskToExecute = &model.TaskNode_TaskDefId{
			TaskDefId: &model.TaskDefId{Name: taskDefNameStr},
		}
	} else {
		taskDefVarAssn, err := t.assignVariable(taskDefName)
		if err != nil {
			t.throwError(err)
		}
		taskNode.TaskToExecute = &model.TaskNode_DynamicTask{
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

func (t *WorkflowThread) executeTask(taskDefName interface{}, args []interface{}) NodeOutput {
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

	node.Node = &model.Node_Task{
		Task: t.createTaskNode(taskDefName, args),
	}

	return NodeOutput{
		nodeName: nodeName,
		thread:   t,
	}
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

	curNode.GetUserTask().Actions = append(curNode.GetUserTask().Actions, &model.UTActionTrigger{
		Hook:         model.UTActionTrigger_ON_TASK_ASSIGNED,
		DelaySeconds: delaySeconds,
		Action: &model.UTActionTrigger_Reassign{
			Reassign: &model.UTActionTrigger_UTAReassign{
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

	var userGroupAssn *model.VariableAssignment
	var userIdAssn *model.VariableAssignment

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

	curNode.GetUserTask().Actions = append(curNode.GetUserTask().Actions, &model.UTActionTrigger{
		Hook:         model.UTActionTrigger_ON_TASK_ASSIGNED,
		DelaySeconds: delaySeconds,
		Action: &model.UTActionTrigger_Reassign{
			Reassign: &model.UTActionTrigger_UTAReassign{
				UserGroup: userGroupAssn,
				UserId:    userIdAssn,
			},
		},
	})
}

func (t *WorkflowThread) scheduleReminderTask(
	userTask *UserTaskOutput, delaySeconds interface{},
	taskDefName string, args ...interface{},
) {
	t.checkIfIsActive()

	delayAssn, err := t.assignVariable(delaySeconds)
	if err != nil {
		log.Fatal(err)
	}

	utaTask := model.UTActionTrigger_Task{
		Task: &model.UTActionTrigger_UTATask{
			Task: t.createTaskNode(taskDefName, args),
		},
	}

	if userTask.Output.nodeName != *(t.lastNodeName) {
		log.Fatal("Tried to edit a stale UserTask node!")
	}

	curNode := t.spec.Nodes[*t.lastNodeName]
	curNode.GetUserTask().Actions = append(curNode.GetUserTask().Actions,
		&model.UTActionTrigger{
			Action:       &utaTask,
			Hook:         model.UTActionTrigger_ON_ARRIVAL,
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

	utaCancel := model.UTActionTrigger_Cancel{
		Cancel: &model.UTActionTrigger_UTACancel{},
	}

	if userTask.Output.nodeName != *(t.lastNodeName) {
		log.Fatal("Tried to edit a stale UserTask node!")
	}
	curNode := t.spec.Nodes[*t.lastNodeName]
	curNode.GetUserTask().Actions = append(curNode.GetUserTask().Actions,
		&model.UTActionTrigger{
			Action:       &utaCancel,
			Hook:         model.UTActionTrigger_ON_ARRIVAL,
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

	utaCancel := model.UTActionTrigger_Cancel{
		Cancel: &model.UTActionTrigger_UTACancel{},
	}

	if userTask.Output.nodeName != *(t.lastNodeName) {
		log.Fatal("Tried to edit a stale UserTask node!")
	}
	curNode := t.spec.Nodes[*t.lastNodeName]
	curNode.GetUserTask().Actions = append(curNode.GetUserTask().Actions,
		&model.UTActionTrigger{
			Action:       &utaCancel,
			Hook:         model.UTActionTrigger_ON_TASK_ASSIGNED,
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

	utaTask := model.UTActionTrigger_Task{
		Task: &model.UTActionTrigger_UTATask{
			Task: t.createTaskNode(taskDefName, args),
		},
	}

	if userTask.Output.nodeName != *(t.lastNodeName) {
		log.Fatal("Tried to edit a stale UserTask node!")
	}

	curNode := t.spec.Nodes[*t.lastNodeName]
	curNode.GetUserTask().Actions = append(curNode.GetUserTask().Actions,
		&model.UTActionTrigger{
			Action:       &utaTask,
			Hook:         model.UTActionTrigger_ON_TASK_ASSIGNED,
			DelaySeconds: delayAssn,
		},
	)
}

func (t *WorkflowThread) assignUserTask(
	userTaskDefName string, userId, userGroup interface{},
) *UserTaskOutput {
	t.checkIfIsActive()

	utNode := &model.UserTaskNode{
		UserTaskDefName: userTaskDefName,
	}

	if userGroup == nil && userId == nil {
		t.throwError(tracerr.Wrap(errors.New(
			"must specify either userGroup or userId when assigning usertask",
		)))
	}

	if userGroup != nil {
		var err error
		userGroupAssn, err := t.assignVariable(userGroup)
		if err != nil {
			t.throwError(tracerr.Wrap(err))
		}
		utNode.UserGroup = userGroupAssn
	}

	if userId != nil {
		userIdAssn, err := t.assignVariable(userId)
		if err != nil {
			t.throwError(tracerr.Wrap(err))
		}
		utNode.UserId = userIdAssn
	}

	nodeName, node := t.createBlankNode(userTaskDefName, "USER_TASK")
	node.Node = &model.Node_UserTask{
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
) (out *model.VariableAssignment, err error) {
	t.checkIfIsActive()
	switch v := val.(type) {
	case *WfRunVariable:
		out = &model.VariableAssignment{
			JsonPath: v.jsonPath,
			Source: &model.VariableAssignment_VariableName{
				VariableName: v.Name,
			},
		}
	case WfRunVariable:
		out = &model.VariableAssignment{
			JsonPath: v.jsonPath,
			Source: &model.VariableAssignment_VariableName{
				VariableName: v.Name,
			},
		}
	case *LHFormatString:
		formatAssignment, _ := t.assignVariable(v.format)
		var argsAssignments []*model.VariableAssignment = make([]*model.VariableAssignment, 0)

		for _, formatArg := range v.formatArgs {
			argAssignment, err := t.assignVariable(formatArg)
			if err != nil {
				t.throwError(tracerr.Wrap(err))
			}
			argsAssignments = append(argsAssignments, argAssignment)
		}
		out = &model.VariableAssignment{
			Source: &model.VariableAssignment_FormatString_{
				FormatString: &model.VariableAssignment_FormatString{
					Format: formatAssignment,
					Args:   argsAssignments,
				},
			},
		}
	case *NodeOutput, NodeOutput:
		err = errors.New(
			"cannot use NodeOutput directly as input to task. Save as var first",
		)
	default:
		var tmp *model.VariableValue
		tmp, err = common.InterfaceToVarVal(v)
		if tmp != nil {
			out = &model.VariableAssignment{
				Source: &model.VariableAssignment_LiteralValue{
					LiteralValue: tmp,
				},
			}
		}
	}

	return out, err
}

func (t *WorkflowThread) createBlankNode(name, nType string) (string, *model.Node) {
	t.checkIfIsActive()
	nodeName := t.getNodeName(name, nType)

	// This should only be called after the 'ENTRYPOINT' node has already been created
	if t.lastNodeName == nil {
		panic("Not possible to have lastNodeName nil at this point")
	}

	// Need to add an edge from that node to this node
	lastNode := t.spec.Nodes[*t.lastNodeName]

	edge := model.Edge{
		SinkNodeName:      nodeName,
		VariableMutations: t.collectVariableMutations(),
	}

	if t.lastNodeCondition != nil {
		edge.Condition = t.lastNodeCondition.spec
		t.lastNodeCondition = nil
	}
	lastNode.OutgoingEdges = append(lastNode.OutgoingEdges, &edge)

	node := &model.Node{
		OutgoingEdges:   make([]*model.Edge, 0),
		FailureHandlers: make([]*model.FailureHandlerDef, 0),
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

func (w *WfRunVariable) searchableImpl() *WfRunVariable {
	w.threadVarDef.Searchable = true
	return w
}

func (w *WfRunVariable) requiredImpl() *WfRunVariable {
	w.threadVarDef.Required = true
	return w
}

// TODO: Add validation that fieldPath is properly set.
func (w *WfRunVariable) searchableOnImpl(fieldPath string, fieldType model.VariableType) *WfRunVariable {
	w.threadVarDef.JsonIndexes = append(w.threadVarDef.JsonIndexes, &model.JsonIndex{
		FieldPath: fieldPath,
		FieldType: fieldType,
	})
	return w
}

func (w *WfRunVariable) withAccessLevel(accessLevel model.WfRunVariableAccessLevel) WfRunVariable {
	w.threadVarDef.AccessLevel = accessLevel
	return *w
}

func (w *WfRunVariable) jsonPathImpl(path string) WfRunVariable {
	if w.jsonPath != nil {
		w.thread.throwError(
			errors.New("Variable " + w.Name + " was jsonpath'ed twice!"),
		)
	}
	if w.VarType != nil && *w.VarType != model.VariableType_JSON_ARR && *w.VarType != model.VariableType_JSON_OBJ {
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

func (t *WorkflowThread) throwError(e error) {
	// For now, we just panic, since it provides a way to get a stacktrace.
	// In the future, we'll do more clean things and try to find out how to
	// bubble up an error with relevant info so that the user can catch it and do
	// stuff with it. But for now, for a demo, panic'ing is fine.
	panic(e)
}

func (t *WorkflowThread) mutate(
	lhs *WfRunVariable,
	mType model.VariableMutationType,
	rhs interface{},
) {
	t.checkIfIsActive()
	mutation := &model.VariableMutation{
		LhsName:     lhs.Name,
		LhsJsonPath: lhs.jsonPath,
		Operation:   mType,
	}

	switch r := rhs.(type) {
	case NodeOutput:
		if r.nodeName != *t.lastNodeName {
			t.throwError(errors.New(
				"Cannot use an old NodeOutput from node " + r.nodeName,
			))
		}
		mutation.RhsValue = &model.VariableMutation_NodeOutput{
			NodeOutput: &model.VariableMutation_NodeOutputSource{
				Jsonpath: r.jsonPath,
			},
		}
	case *NodeOutput:
		if r.nodeName != *t.lastNodeName {
			t.throwError(errors.New(
				"Cannot use an old NodeOutput from node " + r.nodeName,
			))
		}
		mutation.RhsValue = &model.VariableMutation_NodeOutput{
			NodeOutput: &model.VariableMutation_NodeOutputSource{
				Jsonpath: r.jsonPath,
			},
		}
	case WfRunVariable:
		mutation.RhsValue = &model.VariableMutation_SourceVariable{
			SourceVariable: &model.VariableAssignment{
				JsonPath: r.jsonPath,
				Source: &model.VariableAssignment_VariableName{
					VariableName: r.Name,
				},
			},
		}
	case *WfRunVariable:
		mutation.RhsValue = &model.VariableMutation_SourceVariable{
			SourceVariable: &model.VariableAssignment{
				JsonPath: r.jsonPath,
				Source: &model.VariableAssignment_VariableName{
					VariableName: r.Name,
				},
			},
		}
	default:
		rhsVarVal, err := common.InterfaceToVarVal(r)
		if err != nil {
			t.throwError(tracerr.Wrap(err))
		}
		mutation.RhsValue = &model.VariableMutation_LiteralValue{
			LiteralValue: rhsVarVal,
		}
	}

	t.variableMutations = append(t.variableMutations, mutation)
}

func (t *WorkflowThread) addVariable(
	name string, varType model.VariableType, defaultValue interface{},
) *WfRunVariable {
	t.checkIfIsActive()
	varDef := &model.VariableDef{
		Type: varType,
		Name: name,
	}

	if defaultValue != nil {
		defaultVarVal, err := common.InterfaceToVarVal(defaultValue)
		if err != nil {
			log.Fatal(err)
		}
		if *common.GetVarType(defaultVarVal) != varType {
			log.Fatal("provided default value for variable " + name + " didn't match type " + varType.String())
		}
		varDef.DefaultValue = defaultVarVal
	}

	threadVarDef := &model.ThreadVarDef{
		VarDef: varDef,
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
	lhs interface{}, op model.Comparator, rhs interface{},
) *WorkflowCondition {
	t.checkIfIsActive()
	cond := &model.EdgeCondition{
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
	n.Node = &model.Node_Nop{
		Nop: &model.NopNode{},
	}
}

func (t *WorkflowThread) doIf(cond *WorkflowCondition, doIf IfElseBody) {
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
	topOfTreeNode := t.spec.Nodes[*t.lastNodeName]

	// Do the work. This adds the 'A'
	t.lastNodeCondition = cond
	doIf(t)

	// Close off the tree. This creates the B node
	t.addNopNode()

	bottomOfTreeNodeName := t.lastNodeName

	// Now add the sideways path from T directly to B
	topOfTreeNode.OutgoingEdges = append(
		topOfTreeNode.OutgoingEdges,
		&model.Edge{
			SinkNodeName: *bottomOfTreeNodeName,
			Condition:    cond.getReverse(),
		},
	)
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

	ifBlockEdge := model.Edge{
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

func (t *WorkflowThread) collectVariableMutations() []*model.VariableMutation {
	variablesFromIfBlock := make([]*model.VariableMutation, len(t.variableMutations))
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
		&model.Edge{
			SinkNodeName: *bottomOfTreeNodeName,
			Condition:    cond.getReverse(),
		},
	)

	// Now add the sideways path from B directly to T
	bottomOfTreeNode.OutgoingEdges = append(
		bottomOfTreeNode.OutgoingEdges,
		&model.Edge{
			SinkNodeName: *topOfTreeNodeName,
			Condition:    cond.spec,
		},
	)
}

func (c *WorkflowCondition) getReverse() *model.EdgeCondition {
	out := &model.EdgeCondition{}
	out.Left = c.spec.Left
	out.Right = c.spec.Right
	switch c.spec.Comparator {
	case model.Comparator_LESS_THAN:
		out.Comparator = model.Comparator_GREATER_THAN_EQ
	case model.Comparator_GREATER_THAN:
		out.Comparator = model.Comparator_LESS_THAN_EQ
	case model.Comparator_GREATER_THAN_EQ:
		out.Comparator = model.Comparator_LESS_THAN
	case model.Comparator_LESS_THAN_EQ:
		out.Comparator = model.Comparator_GREATER_THAN
	case model.Comparator_EQUALS:
		out.Comparator = model.Comparator_NOT_EQUALS
	case model.Comparator_NOT_EQUALS:
		out.Comparator = model.Comparator_EQUALS
	case model.Comparator_IN:
		out.Comparator = model.Comparator_NOT_IN
	case model.Comparator_NOT_IN:
		out.Comparator = model.Comparator_IN
	}

	return out
}

func (t *WorkflowThread) spawnThread(
	tFunc ThreadFunc, threadName string, args map[string]interface{},
) *SpawnedThread {
	t.checkIfIsActive()
	threadName = t.wf.addSubThread(threadName, tFunc)

	nodeName, node := t.createBlankNode(threadName, "SPAWN_THREAD")
	cachedThreadVar := t.addVariable(nodeName, model.VariableType_INT, nil)

	node.Node = &model.Node_StartThread{
		StartThread: &model.StartThreadNode{
			ThreadSpecName: threadName,
			Variables:      make(map[string]*model.VariableAssignment),
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
		model.VariableMutationType_ASSIGN,
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
	nodeName, node := t.createBlankNode("wait", "WAIT_THREADS")
	node.Node = &model.Node_WaitForThreads{
		WaitForThreads: &model.WaitForThreadsNode{
			ThreadsToWaitFor: &model.WaitForThreadsNode_Threads{
				Threads: &model.WaitForThreadsNode_ThreadsToWaitFor{
					Threads: make([]*model.WaitForThreadsNode_ThreadToWaitFor, 0),
				},
			},
		},
	}

	for _, spawnedThread := range s {
		threadRunNumberAssn, _ := t.assignVariable(spawnedThread.threadNumVar)

		node.GetWaitForThreads().GetThreads().Threads = append(node.GetWaitForThreads().GetThreads().Threads,
			&model.WaitForThreadsNode_ThreadToWaitFor{
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

	if *arrVar.VarType != model.VariableType_JSON_ARR {
		t.throwError(tracerr.Wrap(errors.New("can only iterate over JSON_ARR variable")))
	}

	finalThreadName := t.wf.addSubThread(threadName, threadFunc)
	iterableAssn, err := t.assignVariable(arrVar)
	if err != nil {
		t.throwError(tracerr.Wrap(err))
	}

	subNode := &model.StartMultipleThreadsNode{
		ThreadSpecName: finalThreadName,
		Iterable:       iterableAssn,
		Variables:      make(map[string]*model.VariableAssignment),
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
	node.Node = &model.Node_StartMultipleThreads{
		StartMultipleThreads: subNode,
	}

	internalThreadNumbersVar := t.addVariable(
		nodeName, model.VariableType_JSON_ARR, nil,
	)

	t.mutate(
		internalThreadNumbersVar,
		model.VariableMutationType_ASSIGN,
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

	subNode := &model.WaitForThreadsNode{
		ThreadsToWaitFor: &model.WaitForThreadsNode_ThreadList{
			ThreadList: threadListAssn,
		},
	}

	nodeName, node := t.createBlankNode("threads", "WAIT_FOR_THREADS")
	node.Node = &model.Node_WaitForThreads{
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

	node.Node = &model.Node_ExternalEvent{
		ExternalEvent: &model.ExternalEventNode{
			ExternalEventDefId: &model.ExternalEventDefId{Name: eventName},
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
	node.Node = &model.Node_ThrowEvent{
		ThrowEvent: &model.ThrowEventNode{
			EventDefId: &model.WorkflowEventDefId{
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

	node.Node = &model.Node_Exit{
		Exit: &model.ExitNode{
			FailureDef: &model.FailureDef{
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

	sleepNode := &model.Node_Sleep{
		Sleep: &model.SleepNode{
			SleepLength: &model.SleepNode_RawSeconds{
				RawSeconds: &model.VariableAssignment{
					Source: &model.VariableAssignment_LiteralValue{
						LiteralValue: &model.VariableValue{
							Value: &model.VariableValue_Int{Int: sleepSeconds64},
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
	t.spec.InterruptDefs = append(t.spec.InterruptDefs, &model.InterruptDef{
		ExternalEventDefId: &model.ExternalEventDefId{Name: interruptName},
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

	var fhd *model.FailureHandlerDef

	if specificError != nil {
		failureName := string(*specificError)
		handlerName := "error-handler-" + failureName + "-" + nodeOutput.nodeName
		threadName := t.wf.addSubThread(handlerName, handler)

		fhd = &model.FailureHandlerDef{
			FailureToCatch: &model.FailureHandlerDef_SpecificFailure{
				SpecificFailure: failureName,
			},
			HandlerSpecName: threadName,
		}
	} else {
		handlerName := "error-handler-" + nodeOutput.nodeName
		threadName := t.wf.addSubThread(handlerName, handler)

		fhd = &model.FailureHandlerDef{
			FailureToCatch: &model.FailureHandlerDef_AnyFailureOfType{
				AnyFailureOfType: model.FailureHandlerDef_FAILURE_TYPE_ERROR,
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

	node.FailureHandlers = append(node.FailureHandlers, &model.FailureHandlerDef{
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

	var fhd *model.FailureHandlerDef

	if exceptionName != nil {
		handlerName := "exn-handler-" + *exceptionName + "-" + nodeOutput.nodeName
		threadName := t.wf.addSubThread(handlerName, handler)

		fhd = &model.FailureHandlerDef{
			FailureToCatch: &model.FailureHandlerDef_SpecificFailure{
				SpecificFailure: *exceptionName,
			},
			HandlerSpecName: threadName,
		}
	} else {
		handlerName := "exn-handler-" + nodeOutput.nodeName
		threadName := t.wf.addSubThread(handlerName, handler)

		fhd = &model.FailureHandlerDef{
			FailureToCatch: &model.FailureHandlerDef_AnyFailureOfType{
				AnyFailureOfType: model.FailureHandlerDef_FAILURE_TYPE_EXCEPTION,
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

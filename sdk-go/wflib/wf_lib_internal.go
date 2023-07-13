package wflib

import (
	"errors"
	"strconv"
	"strings"
	"unicode"

	"bitbucket.org/littlehorse-core/littlehorse/sdk-go/common"
	"bitbucket.org/littlehorse-core/littlehorse/sdk-go/common/model"
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

func (l *LHWorkflow) compile() (*model.PutWfSpecPb, error) {
	seenThreads := make(map[string]ThreadFunc)
	l.funcs = make(map[string]ThreadFunc)

	l.spec.Name = camelCaseToHostNameCase(l.Name)
	l.spec.EntrypointThreadName = l.addSubThread("entrypoint", l.EntrypointThread)
	l.spec.ThreadSpecs = make(map[string]*model.ThreadSpecPb)

	for {
		curFuncsSize := len(seenThreads)

		for funcName, function := range l.funcs {
			if _, alreadySeen := seenThreads[funcName]; !alreadySeen {
				funcName = camelCaseToHostNameCase(funcName)
				seenThreads[funcName] = function

				thr := ThreadBuilder{
					Name:     funcName,
					isActive: true,
					wf:       l,
					spec:     model.ThreadSpecPb{},
				}
				thr.spec.InterruptDefs = make([]*model.InterruptDefPb, 0)
				thr.spec.VariableDefs = make([]*model.VariableDefPb, 0)

				// Need to add entrypoint node. We have to do this one manually
				// for now.
				entry := &model.NodePb{
					Node: &model.NodePb_Entrypoint{
						Entrypoint: &model.EntrypointNodePb{},
					},
					OutgoingEdges: make([]*model.EdgePb, 0),
				}
				nodeName := "0-ENTRYPOINT"
				thr.lastNodeName = &nodeName
				thr.lastNodeCondition = &WorkflowCondition{}
				thr.spec.Nodes = make(map[string]*model.NodePb)
				thr.spec.Nodes[nodeName] = entry

				// Now do the work for the thread...this calls the user/customer's
				// code, which has calls to thread.ExecuteTask() etc.
				function(&thr)

				// Now add exit node to make a sandwich
				_, exitNode := thr.createBlankNode("exit", "EXIT")
				exitNode.Node = &model.NodePb_Exit{
					Exit: &model.ExitNodePb{},
				}
				thr.isActive = false
				// Now save the thread to the protobuf
				l.spec.ThreadSpecs[funcName] = &thr.spec
			}

		}
		if curFuncsSize == len(l.funcs) {
			break
		}
	}

	return &l.spec, nil
}

func (t *ThreadBuilder) executeTask(name string, args []interface{}) NodeOutput {
	t.checkIfIsActive()
	nodeName, node := t.createBlankNode(name, "TASK")

	taskNode := &model.NodePb_Task{
		Task: &model.TaskNodePb{
			TaskDefName: name,
			Variables:   make([]*model.VariableAssignmentPb, 0),
		},
	}

	for _, arg := range args {
		varAssn, err := t.assignVariable(arg)
		if err != nil {
			t.throwError(tracerr.Wrap(err))
		}
		taskNode.Task.Variables = append(taskNode.Task.Variables, varAssn)
	}

	node.Node = taskNode

	return NodeOutput{
		nodeName: nodeName,
		thread:   t,
	}
}

func (t *ThreadBuilder) assignVariable(
	val interface{},
) (out *model.VariableAssignmentPb, err error) {
	t.checkIfIsActive()
	switch v := val.(type) {
	case *WfRunVariable:
		out = &model.VariableAssignmentPb{
			JsonPath: v.jsonPath,
			Source: &model.VariableAssignmentPb_VariableName{
				VariableName: v.Name,
			},
		}
	case WfRunVariable:
		out = &model.VariableAssignmentPb{
			JsonPath: v.jsonPath,
			Source: &model.VariableAssignmentPb_VariableName{
				VariableName: v.Name,
			},
		}
	case *NodeOutput, NodeOutput:
		err = errors.New(
			"cannot use NodeOutput directly as input to task. Save as var first",
		)
	default:
		var tmp *model.VariableValuePb
		tmp, err = common.InterfaceToVarVal(v)
		if tmp != nil {
			out = &model.VariableAssignmentPb{
				Source: &model.VariableAssignmentPb_LiteralValue{
					LiteralValue: tmp,
				},
			}
		}
	}

	return out, err
}

func (t *ThreadBuilder) createBlankNode(name, nType string) (string, *model.NodePb) {
	t.checkIfIsActive()
	nodeName := t.getNodeName(name, nType)

	// This should only be called after the 'ENTRYPOINT' node has already been created
	if t.lastNodeName == nil {
		panic("Not possible to have lastNodeName nil at this point")
	}

	// Need to add an edge from that node to this node
	lastNode := t.spec.Nodes[*t.lastNodeName]
	edge := model.EdgePb{
		SinkNodeName: nodeName,
	}
	if t.lastNodeCondition != nil {
		edge.Condition = t.lastNodeCondition.spec
		t.lastNodeCondition = nil
	}
	lastNode.OutgoingEdges = append(lastNode.OutgoingEdges, &edge)

	node := &model.NodePb{
		OutgoingEdges:     make([]*model.EdgePb, 0),
		VariableMutations: make([]*model.VariableMutationPb, 0),
		FailureHandlers:   make([]*model.FailureHandlerDefPb, 0),
	}

	t.spec.Nodes[nodeName] = node
	t.lastNodeName = &nodeName
	return nodeName, node
}

func (t *ThreadBuilder) getNodeName(humanName, nodeType string) string {
	t.checkIfIsActive()
	return strconv.Itoa(len(t.spec.Nodes)) + "-" + humanName + "-" + nodeType
}

func (w *LHWorkflow) addSubThread(threadName string, tf ThreadFunc) string {
	threadName = camelCaseToHostNameCase(threadName)
	w.funcs[threadName] = tf
	return threadName
}

func (w *WfRunVariable) jsonPathImpl(path string) WfRunVariable {
	if w.jsonPath != nil {
		w.thread.throwError(
			errors.New("Variable " + w.Name + " was jsonpath'ed twice!"),
		)
	}
	if w.VarType != nil && *w.VarType != model.VariableTypePb_JSON_ARR && *w.VarType != model.VariableTypePb_JSON_OBJ {
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

func (t *ThreadBuilder) throwError(e error) {
	// For now, we just panic, since it provides a way to get a stacktrace.
	// In the future, we'll do more clean things and try to find out how to
	// bubble up an error with relevant info so that the user can catch it and do
	// stuff with it. But for now, for a demo, panic'ing is fine.
	panic(e)
}

func (t *ThreadBuilder) mutate(
	lhs *WfRunVariable,
	mType model.VariableMutationTypePb,
	rhs interface{},
) {
	t.checkIfIsActive()
	mutation := &model.VariableMutationPb{
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
		mutation.RhsValue = &model.VariableMutationPb_NodeOutput{
			NodeOutput: &model.VariableMutationPb_NodeOutputSourcePb{
				Jsonpath: r.jsonPath,
			},
		}
	case *NodeOutput:
		if r.nodeName != *t.lastNodeName {
			t.throwError(errors.New(
				"Cannot use an old NodeOutput from node " + r.nodeName,
			))
		}
		mutation.RhsValue = &model.VariableMutationPb_NodeOutput{
			NodeOutput: &model.VariableMutationPb_NodeOutputSourcePb{
				Jsonpath: r.jsonPath,
			},
		}
	case WfRunVariable:
		mutation.RhsValue = &model.VariableMutationPb_SourceVariable{
			SourceVariable: &model.VariableAssignmentPb{
				JsonPath: r.jsonPath,
				Source: &model.VariableAssignmentPb_VariableName{
					VariableName: r.Name,
				},
			},
		}
	case *WfRunVariable:
		mutation.RhsValue = &model.VariableMutationPb_SourceVariable{
			SourceVariable: &model.VariableAssignmentPb{
				JsonPath: r.jsonPath,
				Source: &model.VariableAssignmentPb_VariableName{
					VariableName: r.Name,
				},
			},
		}
	default:
		rhsVarVal, err := common.InterfaceToVarVal(r)
		if err != nil {
			t.throwError(tracerr.Wrap(err))
		}
		mutation.RhsValue = &model.VariableMutationPb_LiteralValue{
			LiteralValue: rhsVarVal,
		}
	}

	node := t.spec.Nodes[*t.lastNodeName]
	node.VariableMutations = append(node.VariableMutations, mutation)
}

func (t *ThreadBuilder) addVariable(
	name string, varType model.VariableTypePb,
) *WfRunVariable {
	t.checkIfIsActive()
	varDef := &model.VariableDefPb{
		Type: varType,
		Name: name,
	}
	t.spec.VariableDefs = append(t.spec.VariableDefs, varDef)

	return &WfRunVariable{
		Name:    name,
		VarType: &varType,
		thread:  t,
	}
}

func (t *ThreadBuilder) condition(
	lhs interface{}, op model.ComparatorPb, rhs interface{},
) *WorkflowCondition {
	t.checkIfIsActive()
	cond := &model.EdgeConditionPb{
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
		spec:          cond,
		createdAtNode: *t.lastNodeName,
	}
}

func (t *ThreadBuilder) addNopNode() {
	t.checkIfIsActive()
	_, n := t.createBlankNode("nop", "NOP")
	n.Node = &model.NodePb_Nop{
		Nop: &model.NopNodePb{},
	}
}

func (t *ThreadBuilder) doIf(cond *WorkflowCondition, doIf IfElseBody) {
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
		&model.EdgePb{
			SinkNodeName: *bottomOfTreeNodeName,
			Condition:    cond.getReverse(),
		},
	)
}

func (t *ThreadBuilder) doIfElse(
	cond *WorkflowCondition, doIf IfElseBody, doElse IfElseBody,
) {
	t.checkIfIsActive()

	t.addNopNode()
	treeRootNodeName := t.lastNodeName
	t.lastNodeCondition = cond
	doIf(t)

	t.addNopNode()
	joinerNodeName := t.lastNodeName

	// Go back to the tree root
	t.lastNodeName = treeRootNodeName
	doElse(t)

	if t.lastNodeCondition != nil {
		panic(
			"Impossible (bug): After doing else body, last condition should be nil",
		)
	}

	lastNodeFromElseBlock := t.spec.Nodes[*t.lastNodeName]

	// Make the last node from the else block point to the joiner node
	lastNodeFromElseBlock.OutgoingEdges = append(
		lastNodeFromElseBlock.OutgoingEdges,
		&model.EdgePb{
			SinkNodeName: *joinerNodeName,
		},
	)

	t.lastNodeName = joinerNodeName
}

func (t *ThreadBuilder) doWhile(cond *WorkflowCondition, whileBody ThreadFunc) {
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
		&model.EdgePb{
			SinkNodeName: *bottomOfTreeNodeName,
			Condition:    cond.getReverse(),
		},
	)

	// Now add the sideways path from B directly to T
	bottomOfTreeNode.OutgoingEdges = append(
		bottomOfTreeNode.OutgoingEdges,
		&model.EdgePb{
			SinkNodeName: *topOfTreeNodeName,
			Condition:    cond.spec,
		},
	)
}

func (c *WorkflowCondition) getReverse() *model.EdgeConditionPb {
	out := &model.EdgeConditionPb{}
	out.Left = c.spec.Left
	out.Right = c.spec.Right
	switch c.spec.Comparator {
	case model.ComparatorPb_LESS_THAN:
		out.Comparator = model.ComparatorPb_GREATER_THAN_EQ
	case model.ComparatorPb_GREATER_THAN:
		out.Comparator = model.ComparatorPb_LESS_THAN_EQ
	case model.ComparatorPb_GREATER_THAN_EQ:
		out.Comparator = model.ComparatorPb_LESS_THAN
	case model.ComparatorPb_LESS_THAN_EQ:
		out.Comparator = model.ComparatorPb_GREATER_THAN
	case model.ComparatorPb_EQUALS:
		out.Comparator = model.ComparatorPb_NOT_EQUALS
	case model.ComparatorPb_NOT_EQUALS:
		out.Comparator = model.ComparatorPb_EQUALS
	case model.ComparatorPb_IN:
		out.Comparator = model.ComparatorPb_NOT_IN
	case model.ComparatorPb_NOT_IN:
		out.Comparator = model.ComparatorPb_IN
	}

	return out
}

func (t *ThreadBuilder) spawnThread(
	tFunc ThreadFunc, threadName string, args map[string]interface{},
) *SpawnedThread {
	t.checkIfIsActive()
	threadName = t.wf.addSubThread(threadName, tFunc)

	nodeName, node := t.createBlankNode(threadName, "SPAWN_THREAD")
	cachedThreadVar := t.addVariable(nodeName, model.VariableTypePb_INT)

	node.Node = &model.NodePb_StartThread{
		StartThread: &model.StartThreadNodePb{
			ThreadSpecName: threadName,
			Variables:      make(map[string]*model.VariableAssignmentPb),
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
		model.VariableMutationTypePb_ASSIGN,
		NodeOutput{
			nodeName, nil, t,
		},
	)

	return &SpawnedThread{
		thread:       t,
		threadNumVar: cachedThreadVar,
	}
}

func (t *ThreadBuilder) waitForThreads(s... *SpawnedThread) *NodeOutput {
	t.checkIfIsActive()
	nodeName, node := t.createBlankNode("wait", "WAIT_THREADS")
	node.Node = &model.NodePb_WaitForThreads{
		WaitForThreads: &model.WaitForThreadsNodePb{
			Threads: make([]*model.WaitForThreadsNodePb_ThreadToWaitForPb, 0),
		},
	}

	for _, spawnedThread := range s {
		threadRunNumberAssn, _ := t.assignVariable(spawnedThread.threadNumVar)

		node.GetWaitForThreads().Threads = append(node.GetWaitForThreads().Threads, 
			&model.WaitForThreadsNodePb_ThreadToWaitForPb{
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

func (t *ThreadBuilder) waitForEvent(eventName string) *NodeOutput {
	t.checkIfIsActive()
	nodeName, node := t.createBlankNode(eventName, "EXTERNAL_EVENT")

	node.Node = &model.NodePb_ExternalEvent{
		ExternalEvent: &model.ExternalEventNodePb{
			ExternalEventDefName: eventName,
		},
	}

	return &NodeOutput{
		nodeName: nodeName,
		jsonPath: nil,
		thread:   t,
	}
}

func (t *ThreadBuilder) fail(content interface{}, failureName string, msg *string) {
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

	node.Node = &model.NodePb_Exit{
		Exit: &model.ExitNodePb{
			FailureDef: &model.FailureDefPb{
				FailureName: failureName,
				Content:     contentVarVal,
				Message:     message,
			},
		},
	}
}

func (t *ThreadBuilder) sleep(sleepSeconds int) {
	t.checkIfIsActive()
	_, node := t.createBlankNode("sleep", "SLEEP")

	sleepSeconds64 := int64(sleepSeconds)

	sleepNode := &model.NodePb_Sleep{
		Sleep: &model.SleepNodePb{
			SleepLength: &model.SleepNodePb_RawSeconds{
				RawSeconds: &model.VariableAssignmentPb{
					Source: &model.VariableAssignmentPb_LiteralValue{
						LiteralValue: &model.VariableValuePb{
							Type: model.VariableTypePb_INT,
							Int:  &sleepSeconds64,
						},
					},
				},
			},
		},
	}

	node.Node = sleepNode
}

func (t *ThreadBuilder) handleInterrupt(interruptName string, handler ThreadFunc) {
	t.checkIfIsActive()
	handlerName := t.wf.addSubThread("interrupt-"+interruptName, handler)
	t.spec.InterruptDefs = append(t.spec.InterruptDefs, &model.InterruptDefPb{
		ExternalEventDefName: interruptName,
		HandlerSpecName:      handlerName,
	})
}

func (t *ThreadBuilder) handleException(
	nodeOutput *NodeOutput,
	exceptionName *string,
	handler ThreadFunc,
) {
	t.checkIfIsActive()
	node := t.spec.Nodes[nodeOutput.nodeName]
	handlerName := "exception-handler-" + *exceptionName + "-" + nodeOutput.nodeName
	threadName := t.wf.addSubThread(handlerName, handler)

	node.FailureHandlers = append(node.FailureHandlers, &model.FailureHandlerDefPb{
		SpecificFailure: exceptionName,
		HandlerSpecName: threadName,
	})
}

func (t *ThreadBuilder) checkIfIsActive() {
	if !t.isActive {
		panic("Using a inactive thread")
	}
}

package littlehorse

import "github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"

type lhExpression struct {
	lhs       interface{}
	rhs       interface{}
	operation lhproto.VariableMutationType
}

type castExpression struct {
	source     interface{}
	targetType lhproto.VariableType
}

func (e *castExpression) Add(other interface{}) LHExpression {
	return &lhExpression{lhs: e, rhs: other, operation: lhproto.VariableMutationType_ADD}
}
func (e *castExpression) Subtract(other interface{}) LHExpression {
	return &lhExpression{lhs: e, rhs: other, operation: lhproto.VariableMutationType_SUBTRACT}
}
func (e *castExpression) Multiply(other interface{}) LHExpression {
	return &lhExpression{lhs: e, rhs: other, operation: lhproto.VariableMutationType_MULTIPLY}
}
func (e *castExpression) Divide(other interface{}) LHExpression {
	return &lhExpression{lhs: e, rhs: other, operation: lhproto.VariableMutationType_DIVIDE}
}
func (e *castExpression) Extend(other interface{}) LHExpression {
	return &lhExpression{lhs: e, rhs: other, operation: lhproto.VariableMutationType_EXTEND}
}
func (e *castExpression) RemoveIfPresent(other interface{}) LHExpression {
	return &lhExpression{lhs: e, rhs: other, operation: lhproto.VariableMutationType_REMOVE_IF_PRESENT}
}
func (e *castExpression) RemoveIndex_ByInt(index int) LHExpression {
	return &lhExpression{lhs: e, rhs: index, operation: lhproto.VariableMutationType_REMOVE_INDEX}
}
func (e *castExpression) RemoveIndex_ByExpression(index LHExpression) LHExpression {
	return &lhExpression{lhs: e, rhs: index, operation: lhproto.VariableMutationType_REMOVE_INDEX}
}
func (e *castExpression) RemoveKey(key interface{}) LHExpression {
	return &lhExpression{lhs: e, rhs: key, operation: lhproto.VariableMutationType_REMOVE_KEY}
}
func (e *castExpression) CastTo(targetType lhproto.VariableType) LHExpression {
	return &castExpression{source: e, targetType: targetType}
}
func (e *castExpression) CastToInt() LHExpression    { return e.CastTo(lhproto.VariableType_INT) }
func (e *castExpression) CastToDouble() LHExpression { return e.CastTo(lhproto.VariableType_DOUBLE) }
func (e *castExpression) CastToStr() LHExpression    { return e.CastTo(lhproto.VariableType_STR) }
func (e *castExpression) CastToBool() LHExpression   { return e.CastTo(lhproto.VariableType_BOOL) }
func (e *castExpression) CastToBytes() LHExpression  { return e.CastTo(lhproto.VariableType_BYTES) }
func (e *castExpression) CastToWfRunId() LHExpression {
	return e.CastTo(lhproto.VariableType_WF_RUN_ID)
}

func (e *lhExpression) Add(other interface{}) LHExpression {
	return &lhExpression{
		lhs:       e,
		rhs:       other,
		operation: lhproto.VariableMutationType_ADD,
	}
}

func (e *lhExpression) Subtract(other interface{}) LHExpression {
	return &lhExpression{
		lhs:       e,
		rhs:       other,
		operation: lhproto.VariableMutationType_SUBTRACT,
	}
}

func (e *lhExpression) Multiply(other interface{}) LHExpression {
	return &lhExpression{
		lhs:       e,
		rhs:       other,
		operation: lhproto.VariableMutationType_MULTIPLY,
	}
}

func (e *lhExpression) Divide(other interface{}) LHExpression {
	return &lhExpression{
		lhs:       e,
		rhs:       other,
		operation: lhproto.VariableMutationType_DIVIDE,
	}
}

func (e *lhExpression) Extend(other interface{}) LHExpression {
	return &lhExpression{
		lhs:       e,
		rhs:       other,
		operation: lhproto.VariableMutationType_EXTEND,
	}
}

func (e *lhExpression) RemoveIfPresent(other interface{}) LHExpression {
	return &lhExpression{
		lhs:       e,
		rhs:       other,
		operation: lhproto.VariableMutationType_REMOVE_IF_PRESENT,
	}
}

func (e *lhExpression) RemoveIndex_ByInt(index int) LHExpression {
	return &lhExpression{
		lhs:       e,
		rhs:       index,
		operation: lhproto.VariableMutationType_REMOVE_INDEX,
	}
}

func (e *lhExpression) RemoveIndex_ByExpression(index LHExpression) LHExpression {
	return &lhExpression{
		lhs:       e,
		rhs:       index,
		operation: lhproto.VariableMutationType_REMOVE_INDEX,
	}
}

func (e *lhExpression) RemoveKey(key interface{}) LHExpression {
	return &lhExpression{
		lhs:       e,
		rhs:       key,
		operation: lhproto.VariableMutationType_REMOVE_KEY,
	}
}

func (e *lhExpression) CastTo(targetType lhproto.VariableType) LHExpression {
	return &castExpression{source: e, targetType: targetType}
}
func (e *lhExpression) CastToInt() LHExpression {
	return e.CastTo(lhproto.VariableType_INT)
}
func (e *lhExpression) CastToDouble() LHExpression {
	return e.CastTo(lhproto.VariableType_DOUBLE)
}
func (e *lhExpression) CastToStr() LHExpression {
	return e.CastTo(lhproto.VariableType_STR)
}
func (e *lhExpression) CastToBool() LHExpression {
	return e.CastTo(lhproto.VariableType_BOOL)
}
func (e *lhExpression) CastToBytes() LHExpression {
	return e.CastTo(lhproto.VariableType_BYTES)
}
func (e *lhExpression) CastToWfRunId() LHExpression {
	return e.CastTo(lhproto.VariableType_WF_RUN_ID)
}

// LHExpression methods for TaskNodeOutput
func (n *TaskNodeOutput) Add(other interface{}) LHExpression {
	return &lhExpression{lhs: n, rhs: other, operation: lhproto.VariableMutationType_ADD}
}
func (n *TaskNodeOutput) Subtract(other interface{}) LHExpression {
	return &lhExpression{lhs: n, rhs: other, operation: lhproto.VariableMutationType_SUBTRACT}
}
func (n *TaskNodeOutput) Multiply(other interface{}) LHExpression {
	return &lhExpression{lhs: n, rhs: other, operation: lhproto.VariableMutationType_MULTIPLY}
}
func (n *TaskNodeOutput) Divide(other interface{}) LHExpression {
	return &lhExpression{lhs: n, rhs: other, operation: lhproto.VariableMutationType_DIVIDE}
}
func (n *TaskNodeOutput) Extend(other interface{}) LHExpression {
	return &lhExpression{lhs: n, rhs: other, operation: lhproto.VariableMutationType_EXTEND}
}
func (n *TaskNodeOutput) RemoveIfPresent(other interface{}) LHExpression {
	return &lhExpression{lhs: n, rhs: other, operation: lhproto.VariableMutationType_REMOVE_IF_PRESENT}
}
func (n *TaskNodeOutput) RemoveIndex_ByInt(index int) LHExpression {
	return &lhExpression{lhs: n, rhs: index, operation: lhproto.VariableMutationType_REMOVE_INDEX}
}
func (n *TaskNodeOutput) RemoveIndex_ByExpression(index LHExpression) LHExpression {
	return &lhExpression{lhs: n, rhs: index, operation: lhproto.VariableMutationType_REMOVE_INDEX}
}
func (n *TaskNodeOutput) RemoveKey(key interface{}) LHExpression {
	return &lhExpression{lhs: n, rhs: key, operation: lhproto.VariableMutationType_REMOVE_KEY}
}
func (n *TaskNodeOutput) CastTo(targetType lhproto.VariableType) LHExpression {
	return &castExpression{source: n, targetType: targetType}
}
func (n *TaskNodeOutput) CastToInt() LHExpression {
	return n.CastTo(lhproto.VariableType_INT)
}
func (n *TaskNodeOutput) CastToDouble() LHExpression {
	return n.CastTo(lhproto.VariableType_DOUBLE)
}
func (n *TaskNodeOutput) CastToStr() LHExpression {
	return n.CastTo(lhproto.VariableType_STR)
}
func (n *TaskNodeOutput) CastToBool() LHExpression {
	return n.CastTo(lhproto.VariableType_BOOL)
}
func (n *TaskNodeOutput) CastToBytes() LHExpression {
	return n.CastTo(lhproto.VariableType_BYTES)
}
func (n *TaskNodeOutput) CastToWfRunId() LHExpression {
	return n.CastTo(lhproto.VariableType_WF_RUN_ID)
}

// LHExpression methods for WaitForThreadsNodeOutput
func (n *WaitForThreadsNodeOutput) Add(other interface{}) LHExpression {
	return &lhExpression{lhs: n, rhs: other, operation: lhproto.VariableMutationType_ADD}
}
func (n *WaitForThreadsNodeOutput) Subtract(other interface{}) LHExpression {
	return &lhExpression{lhs: n, rhs: other, operation: lhproto.VariableMutationType_SUBTRACT}
}
func (n *WaitForThreadsNodeOutput) Multiply(other interface{}) LHExpression {
	return &lhExpression{lhs: n, rhs: other, operation: lhproto.VariableMutationType_MULTIPLY}
}
func (n *WaitForThreadsNodeOutput) Divide(other interface{}) LHExpression {
	return &lhExpression{lhs: n, rhs: other, operation: lhproto.VariableMutationType_DIVIDE}
}
func (n *WaitForThreadsNodeOutput) Extend(other interface{}) LHExpression {
	return &lhExpression{lhs: n, rhs: other, operation: lhproto.VariableMutationType_EXTEND}
}
func (n *WaitForThreadsNodeOutput) RemoveIfPresent(other interface{}) LHExpression {
	return &lhExpression{lhs: n, rhs: other, operation: lhproto.VariableMutationType_REMOVE_IF_PRESENT}
}
func (n *WaitForThreadsNodeOutput) RemoveIndex_ByInt(index int) LHExpression {
	return &lhExpression{lhs: n, rhs: index, operation: lhproto.VariableMutationType_REMOVE_INDEX}
}
func (n *WaitForThreadsNodeOutput) RemoveIndex_ByExpression(index LHExpression) LHExpression {
	return &lhExpression{lhs: n, rhs: index, operation: lhproto.VariableMutationType_REMOVE_INDEX}
}
func (n *WaitForThreadsNodeOutput) RemoveKey(key interface{}) LHExpression {
	return &lhExpression{lhs: n, rhs: key, operation: lhproto.VariableMutationType_REMOVE_KEY}
}
func (n *WaitForThreadsNodeOutput) CastTo(targetType lhproto.VariableType) LHExpression {
	return &castExpression{source: n, targetType: targetType}
}
func (n *WaitForThreadsNodeOutput) CastToInt() LHExpression {
	return n.CastTo(lhproto.VariableType_INT)
}
func (n *WaitForThreadsNodeOutput) CastToDouble() LHExpression {
	return n.CastTo(lhproto.VariableType_DOUBLE)
}
func (n *WaitForThreadsNodeOutput) CastToStr() LHExpression {
	return n.CastTo(lhproto.VariableType_STR)
}
func (n *WaitForThreadsNodeOutput) CastToBool() LHExpression {
	return n.CastTo(lhproto.VariableType_BOOL)
}
func (n *WaitForThreadsNodeOutput) CastToBytes() LHExpression {
	return n.CastTo(lhproto.VariableType_BYTES)
}
func (n *WaitForThreadsNodeOutput) CastToWfRunId() LHExpression {
	return n.CastTo(lhproto.VariableType_WF_RUN_ID)
}

// LHExpression methods for ExternalEventNodeOutput
func (n *ExternalEventNodeOutput) Add(other interface{}) LHExpression {
	return &lhExpression{lhs: n, rhs: other, operation: lhproto.VariableMutationType_ADD}
}
func (n *ExternalEventNodeOutput) Subtract(other interface{}) LHExpression {
	return &lhExpression{lhs: n, rhs: other, operation: lhproto.VariableMutationType_SUBTRACT}
}
func (n *ExternalEventNodeOutput) Multiply(other interface{}) LHExpression {
	return &lhExpression{lhs: n, rhs: other, operation: lhproto.VariableMutationType_MULTIPLY}
}
func (n *ExternalEventNodeOutput) Divide(other interface{}) LHExpression {
	return &lhExpression{lhs: n, rhs: other, operation: lhproto.VariableMutationType_DIVIDE}
}
func (n *ExternalEventNodeOutput) Extend(other interface{}) LHExpression {
	return &lhExpression{lhs: n, rhs: other, operation: lhproto.VariableMutationType_EXTEND}
}
func (n *ExternalEventNodeOutput) RemoveIfPresent(other interface{}) LHExpression {
	return &lhExpression{lhs: n, rhs: other, operation: lhproto.VariableMutationType_REMOVE_IF_PRESENT}
}
func (n *ExternalEventNodeOutput) RemoveIndex_ByInt(index int) LHExpression {
	return &lhExpression{lhs: n, rhs: index, operation: lhproto.VariableMutationType_REMOVE_INDEX}
}
func (n *ExternalEventNodeOutput) RemoveIndex_ByExpression(index LHExpression) LHExpression {
	return &lhExpression{lhs: n, rhs: index, operation: lhproto.VariableMutationType_REMOVE_INDEX}
}
func (n *ExternalEventNodeOutput) RemoveKey(key interface{}) LHExpression {
	return &lhExpression{lhs: n, rhs: key, operation: lhproto.VariableMutationType_REMOVE_KEY}
}
func (n *ExternalEventNodeOutput) CastTo(targetType lhproto.VariableType) LHExpression {
	return &castExpression{source: n, targetType: targetType}
}
func (n *ExternalEventNodeOutput) CastToInt() LHExpression {
	return n.CastTo(lhproto.VariableType_INT)
}
func (n *ExternalEventNodeOutput) CastToDouble() LHExpression {
	return n.CastTo(lhproto.VariableType_DOUBLE)
}
func (n *ExternalEventNodeOutput) CastToStr() LHExpression {
	return n.CastTo(lhproto.VariableType_STR)
}
func (n *ExternalEventNodeOutput) CastToBool() LHExpression {
	return n.CastTo(lhproto.VariableType_BOOL)
}
func (n *ExternalEventNodeOutput) CastToBytes() LHExpression {
	return n.CastTo(lhproto.VariableType_BYTES)
}
func (n *ExternalEventNodeOutput) CastToWfRunId() LHExpression {
	return n.CastTo(lhproto.VariableType_WF_RUN_ID)
}

// LHExpression methods for UserTaskNodeOutput
func (n *UserTaskNodeOutput) Add(other interface{}) LHExpression {
	return &lhExpression{lhs: n, rhs: other, operation: lhproto.VariableMutationType_ADD}
}
func (n *UserTaskNodeOutput) Subtract(other interface{}) LHExpression {
	return &lhExpression{lhs: n, rhs: other, operation: lhproto.VariableMutationType_SUBTRACT}
}
func (n *UserTaskNodeOutput) Multiply(other interface{}) LHExpression {
	return &lhExpression{lhs: n, rhs: other, operation: lhproto.VariableMutationType_MULTIPLY}
}
func (n *UserTaskNodeOutput) Divide(other interface{}) LHExpression {
	return &lhExpression{lhs: n, rhs: other, operation: lhproto.VariableMutationType_DIVIDE}
}
func (n *UserTaskNodeOutput) Extend(other interface{}) LHExpression {
	return &lhExpression{lhs: n, rhs: other, operation: lhproto.VariableMutationType_EXTEND}
}
func (n *UserTaskNodeOutput) RemoveIfPresent(other interface{}) LHExpression {
	return &lhExpression{lhs: n, rhs: other, operation: lhproto.VariableMutationType_REMOVE_IF_PRESENT}
}
func (n *UserTaskNodeOutput) RemoveIndex_ByInt(index int) LHExpression {
	return &lhExpression{lhs: n, rhs: index, operation: lhproto.VariableMutationType_REMOVE_INDEX}
}
func (n *UserTaskNodeOutput) RemoveIndex_ByExpression(index LHExpression) LHExpression {
	return &lhExpression{lhs: n, rhs: index, operation: lhproto.VariableMutationType_REMOVE_INDEX}
}
func (n *UserTaskNodeOutput) RemoveKey(key interface{}) LHExpression {
	return &lhExpression{lhs: n, rhs: key, operation: lhproto.VariableMutationType_REMOVE_KEY}
}
func (n *UserTaskNodeOutput) CastTo(targetType lhproto.VariableType) LHExpression {
	return &castExpression{source: n, targetType: targetType}
}
func (n *UserTaskNodeOutput) CastToInt() LHExpression {
	return n.CastTo(lhproto.VariableType_INT)
}
func (n *UserTaskNodeOutput) CastToDouble() LHExpression {
	return n.CastTo(lhproto.VariableType_DOUBLE)
}
func (n *UserTaskNodeOutput) CastToStr() LHExpression {
	return n.CastTo(lhproto.VariableType_STR)
}
func (n *UserTaskNodeOutput) CastToBool() LHExpression {
	return n.CastTo(lhproto.VariableType_BOOL)
}
func (n *UserTaskNodeOutput) CastToBytes() LHExpression {
	return n.CastTo(lhproto.VariableType_BYTES)
}
func (n *UserTaskNodeOutput) CastToWfRunId() LHExpression {
	return n.CastTo(lhproto.VariableType_WF_RUN_ID)
}

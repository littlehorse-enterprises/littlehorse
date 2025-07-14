package littlehorse

import "github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"

type lhExpression struct {
	lhs       interface{}
	rhs       interface{}
	operation lhproto.VariableMutationType
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

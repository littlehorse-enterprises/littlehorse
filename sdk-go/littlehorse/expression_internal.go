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

type comparatorExpression struct {
	lhs        interface{}
	rhs        interface{}
	comparator lhproto.Comparator
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
func (e *castExpression) CastToInt() LHExpression {
	return e.CastTo(lhproto.VariableType_INT)
}
func (e *castExpression) CastToDouble() LHExpression {
	return e.CastTo(lhproto.VariableType_DOUBLE)
}
func (e *castExpression) CastToStr() LHExpression {
	return e.CastTo(lhproto.VariableType_STR)
}
func (e *castExpression) CastToBool() LHExpression {
	return e.CastTo(lhproto.VariableType_BOOL)
}
func (e *castExpression) CastToBytes() LHExpression {
	return e.CastTo(lhproto.VariableType_BYTES)
}
func (e *castExpression) CastToWfRunId() LHExpression {
	return e.CastTo(lhproto.VariableType_WF_RUN_ID)
}

// Comparator methods for castExpression
func (e *castExpression) IsLessThan(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: e, rhs: rhs, comparator: lhproto.Comparator_LESS_THAN}
}
func (e *castExpression) IsGreaterThan(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: e, rhs: rhs, comparator: lhproto.Comparator_GREATER_THAN}
}
func (e *castExpression) IsLessThanEq(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: e, rhs: rhs, comparator: lhproto.Comparator_LESS_THAN_EQ}
}
func (e *castExpression) IsGreaterThanEq(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: e, rhs: rhs, comparator: lhproto.Comparator_GREATER_THAN_EQ}
}
func (e *castExpression) IsEqualTo(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: e, rhs: rhs, comparator: lhproto.Comparator_EQUALS}
}
func (e *castExpression) IsNotEqualTo(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: e, rhs: rhs, comparator: lhproto.Comparator_NOT_EQUALS}
}
func (e *castExpression) DoesContain(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: rhs, rhs: e, comparator: lhproto.Comparator_IN}
}
func (e *castExpression) DoesNotContain(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: rhs, rhs: e, comparator: lhproto.Comparator_NOT_IN}
}
func (e *castExpression) IsIn(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: e, rhs: rhs, comparator: lhproto.Comparator_IN}
}
func (e *castExpression) IsNotIn(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: e, rhs: rhs, comparator: lhproto.Comparator_NOT_IN}
}
func (e *castExpression) And(other interface{}) LHExpression {
	return &lhExpression{lhs: e, rhs: other, operation: lhproto.VariableMutationType_AND}
}
func (e *castExpression) Or(other interface{}) LHExpression {
	return &lhExpression{lhs: e, rhs: other, operation: lhproto.VariableMutationType_OR}
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

// Comparator methods for lhExpression
func (e *lhExpression) IsLessThan(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: e, rhs: rhs, comparator: lhproto.Comparator_LESS_THAN}
}
func (e *lhExpression) IsGreaterThan(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: e, rhs: rhs, comparator: lhproto.Comparator_GREATER_THAN}
}
func (e *lhExpression) IsLessThanEq(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: e, rhs: rhs, comparator: lhproto.Comparator_LESS_THAN_EQ}
}
func (e *lhExpression) IsGreaterThanEq(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: e, rhs: rhs, comparator: lhproto.Comparator_GREATER_THAN_EQ}
}
func (e *lhExpression) IsEqualTo(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: e, rhs: rhs, comparator: lhproto.Comparator_EQUALS}
}
func (e *lhExpression) IsNotEqualTo(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: e, rhs: rhs, comparator: lhproto.Comparator_NOT_EQUALS}
}
func (e *lhExpression) DoesContain(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: rhs, rhs: e, comparator: lhproto.Comparator_IN}
}
func (e *lhExpression) DoesNotContain(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: rhs, rhs: e, comparator: lhproto.Comparator_NOT_IN}
}
func (e *lhExpression) IsIn(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: e, rhs: rhs, comparator: lhproto.Comparator_IN}
}
func (e *lhExpression) IsNotIn(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: e, rhs: rhs, comparator: lhproto.Comparator_NOT_IN}
}
func (e *lhExpression) And(other interface{}) LHExpression {
	return &lhExpression{lhs: e, rhs: other, operation: lhproto.VariableMutationType_AND}
}
func (e *lhExpression) Or(other interface{}) LHExpression {
	return &lhExpression{lhs: e, rhs: other, operation: lhproto.VariableMutationType_OR}
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
func (n *TaskNodeOutput) IsLessThan(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: n, rhs: rhs, comparator: lhproto.Comparator_LESS_THAN}
}
func (n *TaskNodeOutput) IsGreaterThan(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: n, rhs: rhs, comparator: lhproto.Comparator_GREATER_THAN}
}
func (n *TaskNodeOutput) IsLessThanEq(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: n, rhs: rhs, comparator: lhproto.Comparator_LESS_THAN_EQ}
}
func (n *TaskNodeOutput) IsGreaterThanEq(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: n, rhs: rhs, comparator: lhproto.Comparator_GREATER_THAN_EQ}
}
func (n *TaskNodeOutput) IsEqualTo(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: n, rhs: rhs, comparator: lhproto.Comparator_EQUALS}
}
func (n *TaskNodeOutput) IsNotEqualTo(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: n, rhs: rhs, comparator: lhproto.Comparator_NOT_EQUALS}
}
func (n *TaskNodeOutput) DoesContain(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: rhs, rhs: n, comparator: lhproto.Comparator_IN}
}
func (n *TaskNodeOutput) DoesNotContain(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: rhs, rhs: n, comparator: lhproto.Comparator_NOT_IN}
}
func (n *TaskNodeOutput) IsIn(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: n, rhs: rhs, comparator: lhproto.Comparator_IN}
}
func (n *TaskNodeOutput) IsNotIn(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: n, rhs: rhs, comparator: lhproto.Comparator_NOT_IN}
}
func (n *TaskNodeOutput) And(other interface{}) LHExpression {
	return &lhExpression{lhs: n, rhs: other, operation: lhproto.VariableMutationType_AND}
}
func (n *TaskNodeOutput) Or(other interface{}) LHExpression {
	return &lhExpression{lhs: n, rhs: other, operation: lhproto.VariableMutationType_OR}
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
func (n *WaitForThreadsNodeOutput) IsLessThan(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: n, rhs: rhs, comparator: lhproto.Comparator_LESS_THAN}
}
func (n *WaitForThreadsNodeOutput) IsGreaterThan(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: n, rhs: rhs, comparator: lhproto.Comparator_GREATER_THAN}
}
func (n *WaitForThreadsNodeOutput) IsLessThanEq(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: n, rhs: rhs, comparator: lhproto.Comparator_LESS_THAN_EQ}
}
func (n *WaitForThreadsNodeOutput) IsGreaterThanEq(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: n, rhs: rhs, comparator: lhproto.Comparator_GREATER_THAN_EQ}
}
func (n *WaitForThreadsNodeOutput) IsEqualTo(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: n, rhs: rhs, comparator: lhproto.Comparator_EQUALS}
}
func (n *WaitForThreadsNodeOutput) IsNotEqualTo(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: n, rhs: rhs, comparator: lhproto.Comparator_NOT_EQUALS}
}
func (n *WaitForThreadsNodeOutput) DoesContain(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: rhs, rhs: n, comparator: lhproto.Comparator_IN}
}
func (n *WaitForThreadsNodeOutput) DoesNotContain(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: rhs, rhs: n, comparator: lhproto.Comparator_NOT_IN}
}
func (n *WaitForThreadsNodeOutput) IsIn(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: n, rhs: rhs, comparator: lhproto.Comparator_IN}
}
func (n *WaitForThreadsNodeOutput) IsNotIn(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: n, rhs: rhs, comparator: lhproto.Comparator_NOT_IN}
}
func (n *WaitForThreadsNodeOutput) And(other interface{}) LHExpression {
	return &lhExpression{lhs: n, rhs: other, operation: lhproto.VariableMutationType_AND}
}
func (n *WaitForThreadsNodeOutput) Or(other interface{}) LHExpression {
	return &lhExpression{lhs: n, rhs: other, operation: lhproto.VariableMutationType_OR}
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
func (n *ExternalEventNodeOutput) IsLessThan(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: n, rhs: rhs, comparator: lhproto.Comparator_LESS_THAN}
}
func (n *ExternalEventNodeOutput) IsGreaterThan(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: n, rhs: rhs, comparator: lhproto.Comparator_GREATER_THAN}
}
func (n *ExternalEventNodeOutput) IsLessThanEq(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: n, rhs: rhs, comparator: lhproto.Comparator_LESS_THAN_EQ}
}
func (n *ExternalEventNodeOutput) IsGreaterThanEq(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: n, rhs: rhs, comparator: lhproto.Comparator_GREATER_THAN_EQ}
}
func (n *ExternalEventNodeOutput) IsEqualTo(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: n, rhs: rhs, comparator: lhproto.Comparator_EQUALS}
}
func (n *ExternalEventNodeOutput) IsNotEqualTo(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: n, rhs: rhs, comparator: lhproto.Comparator_NOT_EQUALS}
}
func (n *ExternalEventNodeOutput) DoesContain(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: rhs, rhs: n, comparator: lhproto.Comparator_IN}
}
func (n *ExternalEventNodeOutput) DoesNotContain(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: rhs, rhs: n, comparator: lhproto.Comparator_NOT_IN}
}
func (n *ExternalEventNodeOutput) IsIn(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: n, rhs: rhs, comparator: lhproto.Comparator_IN}
}
func (n *ExternalEventNodeOutput) IsNotIn(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: n, rhs: rhs, comparator: lhproto.Comparator_NOT_IN}
}
func (n *ExternalEventNodeOutput) And(other interface{}) LHExpression {
	return &lhExpression{lhs: n, rhs: other, operation: lhproto.VariableMutationType_AND}
}
func (n *ExternalEventNodeOutput) Or(other interface{}) LHExpression {
	return &lhExpression{lhs: n, rhs: other, operation: lhproto.VariableMutationType_OR}
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
func (n *UserTaskNodeOutput) IsLessThan(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: n, rhs: rhs, comparator: lhproto.Comparator_LESS_THAN}
}
func (n *UserTaskNodeOutput) IsGreaterThan(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: n, rhs: rhs, comparator: lhproto.Comparator_GREATER_THAN}
}
func (n *UserTaskNodeOutput) IsLessThanEq(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: n, rhs: rhs, comparator: lhproto.Comparator_LESS_THAN_EQ}
}
func (n *UserTaskNodeOutput) IsGreaterThanEq(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: n, rhs: rhs, comparator: lhproto.Comparator_GREATER_THAN_EQ}
}
func (n *UserTaskNodeOutput) IsEqualTo(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: n, rhs: rhs, comparator: lhproto.Comparator_EQUALS}
}
func (n *UserTaskNodeOutput) IsNotEqualTo(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: n, rhs: rhs, comparator: lhproto.Comparator_NOT_EQUALS}
}
func (n *UserTaskNodeOutput) DoesContain(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: rhs, rhs: n, comparator: lhproto.Comparator_IN}
}
func (n *UserTaskNodeOutput) DoesNotContain(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: rhs, rhs: n, comparator: lhproto.Comparator_NOT_IN}
}
func (n *UserTaskNodeOutput) IsIn(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: n, rhs: rhs, comparator: lhproto.Comparator_IN}
}
func (n *UserTaskNodeOutput) IsNotIn(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: n, rhs: rhs, comparator: lhproto.Comparator_NOT_IN}
}
func (n *UserTaskNodeOutput) And(other interface{}) LHExpression {
	return &lhExpression{lhs: n, rhs: other, operation: lhproto.VariableMutationType_AND}
}
func (n *UserTaskNodeOutput) Or(other interface{}) LHExpression {
	return &lhExpression{lhs: n, rhs: other, operation: lhproto.VariableMutationType_OR}
}

// Arithmetic methods
func (e *comparatorExpression) Add(other interface{}) LHExpression {
	return &lhExpression{lhs: e, rhs: other, operation: lhproto.VariableMutationType_ADD}
}
func (e *comparatorExpression) Subtract(other interface{}) LHExpression {
	return &lhExpression{lhs: e, rhs: other, operation: lhproto.VariableMutationType_SUBTRACT}
}
func (e *comparatorExpression) Multiply(other interface{}) LHExpression {
	return &lhExpression{lhs: e, rhs: other, operation: lhproto.VariableMutationType_MULTIPLY}
}
func (e *comparatorExpression) Divide(other interface{}) LHExpression {
	return &lhExpression{lhs: e, rhs: other, operation: lhproto.VariableMutationType_DIVIDE}
}
func (e *comparatorExpression) Extend(other interface{}) LHExpression {
	return &lhExpression{lhs: e, rhs: other, operation: lhproto.VariableMutationType_EXTEND}
}
func (e *comparatorExpression) RemoveIfPresent(other interface{}) LHExpression {
	return &lhExpression{lhs: e, rhs: other, operation: lhproto.VariableMutationType_REMOVE_IF_PRESENT}
}
func (e *comparatorExpression) RemoveIndex_ByInt(index int) LHExpression {
	return &lhExpression{lhs: e, rhs: index, operation: lhproto.VariableMutationType_REMOVE_INDEX}
}
func (e *comparatorExpression) RemoveIndex_ByExpression(index LHExpression) LHExpression {
	return &lhExpression{lhs: e, rhs: index, operation: lhproto.VariableMutationType_REMOVE_INDEX}
}
func (e *comparatorExpression) RemoveKey(key interface{}) LHExpression {
	return &lhExpression{lhs: e, rhs: key, operation: lhproto.VariableMutationType_REMOVE_KEY}
}

// Cast methods
func (e *comparatorExpression) CastTo(targetType lhproto.VariableType) LHExpression {
	return &castExpression{source: e, targetType: targetType}
}
func (e *comparatorExpression) CastToInt() LHExpression {
	return e.CastTo(lhproto.VariableType_INT)
}
func (e *comparatorExpression) CastToDouble() LHExpression {
	return e.CastTo(lhproto.VariableType_DOUBLE)
}
func (e *comparatorExpression) CastToStr() LHExpression {
	return e.CastTo(lhproto.VariableType_STR)
}
func (e *comparatorExpression) CastToBool() LHExpression {
	return e.CastTo(lhproto.VariableType_BOOL)
}
func (e *comparatorExpression) CastToBytes() LHExpression {
	return e.CastTo(lhproto.VariableType_BYTES)
}
func (e *comparatorExpression) CastToWfRunId() LHExpression {
	return e.CastTo(lhproto.VariableType_WF_RUN_ID)
}

// Comparator methods
func (e *comparatorExpression) IsLessThan(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: e, rhs: rhs, comparator: lhproto.Comparator_LESS_THAN}
}
func (e *comparatorExpression) IsGreaterThan(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: e, rhs: rhs, comparator: lhproto.Comparator_GREATER_THAN}
}
func (e *comparatorExpression) IsLessThanEq(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: e, rhs: rhs, comparator: lhproto.Comparator_LESS_THAN_EQ}
}
func (e *comparatorExpression) IsGreaterThanEq(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: e, rhs: rhs, comparator: lhproto.Comparator_GREATER_THAN_EQ}
}
func (e *comparatorExpression) IsEqualTo(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: e, rhs: rhs, comparator: lhproto.Comparator_EQUALS}
}
func (e *comparatorExpression) IsNotEqualTo(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: e, rhs: rhs, comparator: lhproto.Comparator_NOT_EQUALS}
}
func (e *comparatorExpression) DoesContain(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: rhs, rhs: e, comparator: lhproto.Comparator_IN}
}
func (e *comparatorExpression) DoesNotContain(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: rhs, rhs: e, comparator: lhproto.Comparator_NOT_IN}
}
func (e *comparatorExpression) IsIn(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: e, rhs: rhs, comparator: lhproto.Comparator_IN}
}
func (e *comparatorExpression) IsNotIn(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: e, rhs: rhs, comparator: lhproto.Comparator_NOT_IN}
}

// Boolean logic methods
func (e *comparatorExpression) And(other interface{}) LHExpression {
	return &lhExpression{lhs: e, rhs: other, operation: lhproto.VariableMutationType_AND}
}
func (e *comparatorExpression) Or(other interface{}) LHExpression {
	return &lhExpression{lhs: e, rhs: other, operation: lhproto.VariableMutationType_OR}
}

// LHExpression methods for WorkflowCondition (deprecated backward compat)
func (c *WorkflowCondition) Add(other interface{}) LHExpression {
	return &lhExpression{lhs: c, rhs: other, operation: lhproto.VariableMutationType_ADD}
}
func (c *WorkflowCondition) Subtract(other interface{}) LHExpression {
	return &lhExpression{lhs: c, rhs: other, operation: lhproto.VariableMutationType_SUBTRACT}
}
func (c *WorkflowCondition) Multiply(other interface{}) LHExpression {
	return &lhExpression{lhs: c, rhs: other, operation: lhproto.VariableMutationType_MULTIPLY}
}
func (c *WorkflowCondition) Divide(other interface{}) LHExpression {
	return &lhExpression{lhs: c, rhs: other, operation: lhproto.VariableMutationType_DIVIDE}
}
func (c *WorkflowCondition) Extend(other interface{}) LHExpression {
	return &lhExpression{lhs: c, rhs: other, operation: lhproto.VariableMutationType_EXTEND}
}
func (c *WorkflowCondition) RemoveIfPresent(other interface{}) LHExpression {
	return &lhExpression{lhs: c, rhs: other, operation: lhproto.VariableMutationType_REMOVE_IF_PRESENT}
}
func (c *WorkflowCondition) RemoveIndex_ByInt(index int) LHExpression {
	return &lhExpression{lhs: c, rhs: index, operation: lhproto.VariableMutationType_REMOVE_INDEX}
}
func (c *WorkflowCondition) RemoveIndex_ByExpression(index LHExpression) LHExpression {
	return &lhExpression{lhs: c, rhs: index, operation: lhproto.VariableMutationType_REMOVE_INDEX}
}
func (c *WorkflowCondition) RemoveKey(key interface{}) LHExpression {
	return &lhExpression{lhs: c, rhs: key, operation: lhproto.VariableMutationType_REMOVE_KEY}
}
func (c *WorkflowCondition) CastTo(targetType lhproto.VariableType) LHExpression {
	return &castExpression{source: c, targetType: targetType}
}
func (c *WorkflowCondition) CastToInt() LHExpression {
	return c.CastTo(lhproto.VariableType_INT)
}
func (c *WorkflowCondition) CastToDouble() LHExpression {
	return c.CastTo(lhproto.VariableType_DOUBLE)
}
func (c *WorkflowCondition) CastToStr() LHExpression {
	return c.CastTo(lhproto.VariableType_STR)
}
func (c *WorkflowCondition) CastToBool() LHExpression {
	return c.CastTo(lhproto.VariableType_BOOL)
}
func (c *WorkflowCondition) CastToBytes() LHExpression {
	return c.CastTo(lhproto.VariableType_BYTES)
}
func (c *WorkflowCondition) CastToWfRunId() LHExpression {
	return c.CastTo(lhproto.VariableType_WF_RUN_ID)
}
func (c *WorkflowCondition) IsLessThan(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: c, rhs: rhs, comparator: lhproto.Comparator_LESS_THAN}
}
func (c *WorkflowCondition) IsGreaterThan(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: c, rhs: rhs, comparator: lhproto.Comparator_GREATER_THAN}
}
func (c *WorkflowCondition) IsLessThanEq(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: c, rhs: rhs, comparator: lhproto.Comparator_LESS_THAN_EQ}
}
func (c *WorkflowCondition) IsGreaterThanEq(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: c, rhs: rhs, comparator: lhproto.Comparator_GREATER_THAN_EQ}
}
func (c *WorkflowCondition) IsEqualTo(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: c, rhs: rhs, comparator: lhproto.Comparator_EQUALS}
}
func (c *WorkflowCondition) IsNotEqualTo(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: c, rhs: rhs, comparator: lhproto.Comparator_NOT_EQUALS}
}
func (c *WorkflowCondition) DoesContain(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: rhs, rhs: c, comparator: lhproto.Comparator_IN}
}
func (c *WorkflowCondition) DoesNotContain(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: rhs, rhs: c, comparator: lhproto.Comparator_NOT_IN}
}
func (c *WorkflowCondition) IsIn(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: c, rhs: rhs, comparator: lhproto.Comparator_IN}
}
func (c *WorkflowCondition) IsNotIn(rhs interface{}) LHExpression {
	return &comparatorExpression{lhs: c, rhs: rhs, comparator: lhproto.Comparator_NOT_IN}
}
func (c *WorkflowCondition) And(other interface{}) LHExpression {
	return &lhExpression{lhs: c, rhs: other, operation: lhproto.VariableMutationType_AND}
}
func (c *WorkflowCondition) Or(other interface{}) LHExpression {
	return &lhExpression{lhs: c, rhs: other, operation: lhproto.VariableMutationType_OR}
}

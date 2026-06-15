import { Comparator, VariableAssignment } from 'littlehorse-client/proto'
import { formatNodeOutputSourceLabel, getEdgeOperandDisplayText, getOperandDisplayText, getTruthyConditionDisplayText, hasResolvedNodeOutput, isBranchEdgeReached, isNopConditionalBranch, parseEdgeCondition } from './edgeConditionDisplay'

describe('formatNodeOutputSourceLabel', () => {
  it('extracts task name from TASK node ids', () => {
    expect(formatNodeOutputSourceLabel('1-greet-TASK')).toBe('greet')
    expect(formatNodeOutputSourceLabel('2-fetch-address-TASK')).toBe('fetch-address')
  })

  it('returns the original name for unknown patterns', () => {
    expect(formatNodeOutputSourceLabel('2-nop-NOP')).toBe('2-nop-NOP')
  })
})

describe('hasResolvedNodeOutput', () => {
  const assignment: VariableAssignment = {
    source: { $case: 'nodeOutput', value: { nodeName: '1-greet-TASK' } },
  }

  it('is true when runtime value exists', () => {
    expect(
      hasResolvedNodeOutput(assignment, {
        nodeOutputValues: {
          '1-greet-TASK': { value: { $case: 'str', value: 'hi' } },
        },
      })
    ).toBe(true)
  })

  it('is false without runtime context', () => {
    expect(hasResolvedNodeOutput(assignment, undefined)).toBe(false)
  })
})

describe('isNopConditionalBranch', () => {
  it('is true when a nop has multiple edges and one is conditional', () => {
    expect(
      isNopConditionalBranch([
        { edgeCondition: { $case: 'legacyCondition', value: {} } },
        { edgeCondition: undefined },
      ])
    ).toBe(true)
  })

  it('is false for a single outgoing edge', () => {
    expect(isNopConditionalBranch([{ edgeCondition: { $case: 'legacyCondition', value: {} } }])).toBe(false)
  })
})

describe('parseEdgeCondition', () => {
  it('parses legacy comparator conditions', () => {
    const assignment: VariableAssignment = {
      source: { $case: 'nodeOutput', value: { nodeName: '1-greet-TASK' } },
    }
    const parsed = parseEdgeCondition({
      $case: 'legacyCondition',
      value: {
        left: assignment,
        comparator: Comparator.EQUALS,
        right: { source: { $case: 'literalValue', value: { value: { $case: 'str', value: 'hi' } } } },
      },
    })
    expect(parsed?.leftOperand).toEqual(assignment)
    expect(parsed?.operatorSymbol).toBe('==')
  })

  it('parses BOOL variable truthiness conditions', () => {
    const assignment: VariableAssignment = {
      source: { $case: 'variableName', value: 'enabled' },
    }
    const parsed = parseEdgeCondition({
      $case: 'condition',
      value: assignment,
    })
    expect(parsed?.isTruthyCheck).toBe(true)
    expect(parsed?.leftOperand).toEqual(assignment)
    expect(getTruthyConditionDisplayText(assignment)).toBe('enabled')
  })

  it('formats variable comparisons without WfSpec braces', () => {
    const assignment: VariableAssignment = {
      source: { $case: 'variableName', value: 'tier' },
    }
    expect(getEdgeOperandDisplayText(assignment)).toBe('tier')
  })
})

describe('isBranchEdgeReached', () => {
  it('is always true in WfSpec view', () => {
    expect(isBranchEdgeReached('2-greet-TASK', undefined)).toBe(true)
  })

  it('is true when the target node has a NodeRun', () => {
    expect(isBranchEdgeReached('2-greet-TASK', [{ nodeName: '2-greet-TASK' }])).toBe(true)
  })

  it('is false when the target node was not executed', () => {
    expect(isBranchEdgeReached('3-greet-TASK', [{ nodeName: '2-nop-NOP' }])).toBe(false)
  })
})

describe('getOperandDisplayText', () => {
  it('prefers source label when requested', () => {
    const assignment: VariableAssignment = {
      source: { $case: 'nodeOutput', value: { nodeName: '1-greet-TASK' } },
    }
    expect(getOperandDisplayText(assignment, undefined, { preferSourceLabel: true })).toBe('greet')
  })
})

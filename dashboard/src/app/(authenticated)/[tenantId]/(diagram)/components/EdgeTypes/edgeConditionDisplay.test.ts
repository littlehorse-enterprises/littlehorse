import { Comparator, VariableAssignment } from 'littlehorse-client/proto'
import {
  formatNodeOutputSourceLabel,
  getEdgeOperandDisplayText,
  getOperandDisplayText,
  getTruthyConditionDisplayText,
  hasResolvedNodeOutput,
  isBranchEdgeReached,
  isNopConditionalBranch,
  parseEdgeCondition,
} from './edgeConditionDisplay'

const nodeOutput: VariableAssignment = {
  source: { oneofKind: 'nodeOutput', nodeOutput: { nodeName: '1-greet-TASK' } },
  path: { oneofKind: undefined },
} as VariableAssignment

const variable = (name: string): VariableAssignment =>
  ({ source: { oneofKind: 'variableName', variableName: name }, path: { oneofKind: undefined } }) as VariableAssignment

const literal = (str: string): VariableAssignment =>
  ({
    source: { oneofKind: 'literalValue', literalValue: { value: { oneofKind: 'str', str } } },
    path: { oneofKind: undefined },
  }) as VariableAssignment

describe('formatNodeOutputSourceLabel', () => {
  it('extracts the task name from TASK node ids', () => {
    expect(formatNodeOutputSourceLabel('1-greet-TASK')).toBe('greet')
    expect(formatNodeOutputSourceLabel('2-fetch-address-TASK')).toBe('fetch-address')
  })

  it('returns the original name for other node types', () => {
    expect(formatNodeOutputSourceLabel('2-nop-NOP')).toBe('2-nop-NOP')
  })
})

describe('hasResolvedNodeOutput', () => {
  it('is true when a runtime value exists', () => {
    expect(
      hasResolvedNodeOutput(nodeOutput, {
        nodeOutputValues: { '1-greet-TASK': { value: { oneofKind: 'str', str: 'hi' } } },
      })
    ).toBe(true)
  })

  it('is false without runtime context', () => {
    expect(hasResolvedNodeOutput(nodeOutput)).toBe(false)
  })
})

describe('isNopConditionalBranch', () => {
  it('is true when a nop has several edges and one is conditional', () => {
    expect(
      isNopConditionalBranch([
        { edgeCondition: { oneofKind: 'condition', condition: variable('enabled') } },
        { edgeCondition: { oneofKind: undefined } },
      ])
    ).toBe(true)
  })

  it('is false for a single outgoing edge', () => {
    expect(
      isNopConditionalBranch([{ edgeCondition: { oneofKind: 'condition', condition: variable('enabled') } }])
    ).toBe(false)
  })
})

describe('parseEdgeCondition', () => {
  it('parses legacy comparator conditions', () => {
    const parsed = parseEdgeCondition({
      oneofKind: 'legacyCondition',
      legacyCondition: { left: nodeOutput, comparator: Comparator.EQUALS, right: literal('hi') },
    })
    expect(parsed?.leftOperand).toEqual(nodeOutput)
    expect(parsed?.operatorSymbol).toBe('==')
  })

  it('parses expression conditions', () => {
    const parsed = parseEdgeCondition({
      oneofKind: 'condition',
      condition: {
        source: {
          oneofKind: 'expression',
          expression: {
            lhs: variable('score'),
            rhs: literal('10'),
            operation: { oneofKind: 'comparator', comparator: Comparator.LESS_THAN },
          },
        },
        path: { oneofKind: undefined },
      } as VariableAssignment,
    })
    expect(parsed?.operatorSymbol).toBe('<')
    expect(parsed?.isTruthyCheck).toBeUndefined()
  })

  it('parses BOOL variable truthiness conditions', () => {
    const parsed = parseEdgeCondition({ oneofKind: 'condition', condition: variable('enabled') })
    expect(parsed?.isTruthyCheck).toBe(true)
    expect(getTruthyConditionDisplayText(variable('enabled'))).toBe('enabled')
  })

  it('is null without a condition', () => {
    expect(parseEdgeCondition({ oneofKind: undefined })).toBeNull()
    expect(parseEdgeCondition(undefined)).toBeNull()
  })
})

describe('operand display text', () => {
  it('formats variable comparisons without WfSpec braces', () => {
    expect(getEdgeOperandDisplayText(variable('tier'))).toBe('tier')
  })

  it('prefers the source label when requested', () => {
    expect(getOperandDisplayText(nodeOutput, undefined, { preferSourceLabel: true })).toBe('greet')
  })

  it('shows the resolved output when runtime context is provided', () => {
    expect(
      getEdgeOperandDisplayText(nodeOutput, {
        nodeOutputValues: { '1-greet-TASK': { value: { oneofKind: 'str', str: 'hello there, Obi-Wan' } } },
      })
    ).toBe('hello there, Obi-Wan')
  })
})

describe('isBranchEdgeReached', () => {
  it('is always true in the WfSpec view', () => {
    expect(isBranchEdgeReached('2-greet-TASK', undefined)).toBe(true)
  })

  it('is true when the target node has a NodeRun', () => {
    expect(isBranchEdgeReached('2-greet-TASK', [{ nodeName: '2-greet-TASK' }])).toBe(true)
  })

  it('is false when the target node was not executed', () => {
    expect(isBranchEdgeReached('3-greet-TASK', [{ nodeName: '2-nop-NOP' }])).toBe(false)
  })
})

import { Edge, NodeRun, TaskAttempt, VariableAssignment, VariableMutation, WfSpec } from 'littlehorse-client/proto'
import { buildNodeOutputValuesFromNodeRuns, getConditionTaskRunIds, taskAttemptOutput } from './taskRunOutput'

const output = (value: string): TaskAttempt =>
  ({
    result: { oneofKind: 'output', output: { value: { oneofKind: 'str', str: value } } },
  }) as TaskAttempt

const error: TaskAttempt = { result: { oneofKind: 'error', error: { message: 'fail', type: 0 } } } as TaskAttempt

describe('taskAttemptOutput', () => {
  it('returns the latest successful output', () => {
    const result = taskAttemptOutput([error, output('hello there, Obi-Wan')])
    expect(result?.value).toEqual({ oneofKind: 'str', str: 'hello there, Obi-Wan' })
  })

  it('is undefined when no attempt produced output', () => {
    expect(taskAttemptOutput([error])).toBeUndefined()
  })
})

describe('buildNodeOutputValuesFromNodeRuns', () => {
  it('maps node names to task outputs', () => {
    const values = buildNodeOutputValuesFromNodeRuns(
      [
        {
          nodeName: '1-greet-TASK',
          nodeType: { oneofKind: 'task', task: { taskRunId: { wfRunId: { id: 'x' }, taskGuid: '0-1' } } },
        } as never,
        { nodeName: '2-nop-NOP', nodeType: { oneofKind: 'nop', nop: {} } } as never,
      ],
      new Map([['0-1', { attempts: [output('hello there, Obi-Wan')] }]])
    )
    expect(Object.keys(values)).toEqual(['1-greet-TASK'])
    expect(values['1-greet-TASK'].value).toEqual({ oneofKind: 'str', str: 'hello there, Obi-Wan' })
  })
})

const reference = (nodeName: string): VariableAssignment =>
  VariableAssignment.create({ source: { oneofKind: 'nodeOutput', nodeOutput: { nodeName } } })

const specWithConditions = (...conditions: Edge['edgeCondition'][]): WfSpec =>
  WfSpec.create({
    threadSpecs: {
      main: { nodes: { branch: { outgoingEdges: conditions.map(edgeCondition => ({ edgeCondition })) } } },
    },
  })

const taskNodeRun = (nodeName: string, position: number, threadRunNumber = 0, threadSpecName = 'main'): NodeRun =>
  NodeRun.create({
    id: { wfRunId: { id: 'run' }, threadRunNumber, position },
    nodeName,
    threadSpecName,
    nodeType: {
      oneofKind: 'task',
      task: { taskRunId: { wfRunId: { id: 'run' }, taskGuid: `${threadRunNumber}-${position}` } },
    },
  })

describe('getConditionTaskRunIds', () => {
  const guids = (spec: WfSpec, runs: NodeRun[]) => getConditionTaskRunIds(spec, runs).map(id => id.taskGuid)

  it('selects both legacy operands and skips tasks used only in mutations', () => {
    const spec = specWithConditions({
      oneofKind: 'legacyCondition',
      legacyCondition: { left: reference('left'), right: reference('right'), comparator: 0 },
    })
    spec.threadSpecs.main.nodes.branch.outgoingEdges[0].variableMutations.push(
      VariableMutation.create({
        lhsName: 'value',
        rhsValue: { oneofKind: 'rhsAssignment', rhsAssignment: reference('unused') },
      })
    )
    expect(guids(spec, [taskNodeRun('left', 1), taskNodeRun('right', 2), taskNodeRun('unused', 3)])).toEqual([
      '0-1',
      '0-2',
    ])
  })

  it.each([
    [
      'expression',
      VariableAssignment.create({
        source: {
          oneofKind: 'expression',
          expression: {
            lhs: reference('left'),
            operation: { oneofKind: 'comparator', comparator: 0 },
            rhs: VariableAssignment.create({
              source: { oneofKind: 'sizeOf', sizeOf: { operand: reference('right') } },
            }),
          },
        },
      }),
    ],
    [
      'format string',
      VariableAssignment.create({
        source: {
          oneofKind: 'formatString',
          formatString: {
            format: reference('left'),
            args: [reference('right')],
          },
        },
      }),
    ],
    [
      'map',
      VariableAssignment.create({
        source: {
          oneofKind: 'mapBuilder',
          mapBuilder: {
            entries: [{ key: reference('left'), value: reference('right') }],
          },
        },
      }),
    ],
    [
      'struct',
      VariableAssignment.create({
        source: {
          oneofKind: 'structBuilder',
          structBuilder: {
            value: {
              fields: {
                first: { structValue: { oneofKind: 'simpleValue', simpleValue: reference('left') } },
                nested: {
                  structValue: {
                    oneofKind: 'subStructure',
                    subStructure: {
                      fields: {
                        second: { structValue: { oneofKind: 'simpleValue', simpleValue: reference('right') } },
                      },
                    },
                  },
                },
              },
            },
          },
        },
      }),
    ],
    [
      'dynamic path',
      VariableAssignment.create({
        ...reference('left'),
        path: {
          oneofKind: 'lhPath',
          lhPath: { path: [{ selectorType: { oneofKind: 'dynamic', dynamic: reference('right') } }] },
        },
      }),
    ],
  ])('finds nested references in a %s', (_name, condition) => {
    const spec = specWithConditions({ oneofKind: 'condition', condition })
    expect(guids(spec, [taskNodeRun('left', 1), taskNodeRun('right', 2), taskNodeRun('unused', 3)])).toEqual([
      '0-1',
      '0-2',
    ])
  })

  it('does not fetch outputs for variable-only conditions', () => {
    const spec = specWithConditions({
      oneofKind: 'condition',
      condition: VariableAssignment.create({ source: { oneofKind: 'variableName', variableName: 'left' } }),
    })
    expect(guids(spec, [taskNodeRun('left', 1)])).toEqual([])
  })

  it('fetches the latest loop execution once, regardless of node-run ordering', () => {
    const spec = specWithConditions(
      { oneofKind: 'condition', condition: reference('loop') },
      { oneofKind: 'condition', condition: reference('loop') }
    )
    const newest = taskNodeRun('loop', 5)
    expect(guids(spec, [newest, taskNodeRun('loop', 1), newest])).toEqual(['0-5'])
  })

  it('keeps thread instances separate and scopes references to their thread spec', () => {
    const spec = specWithConditions({ oneofKind: 'condition', condition: reference('task') })
    spec.threadSpecs.other = WfSpec.create({ threadSpecs: { other: {} } }).threadSpecs.other
    expect(
      guids(spec, [taskNodeRun('task', 1), taskNodeRun('task', 2, 1), taskNodeRun('task', 3, 2, 'other')])
    ).toEqual(['0-1', '1-2'])
  })

  it('does not substitute an older loop output when the latest execution has no task yet', () => {
    const spec = specWithConditions({ oneofKind: 'condition', condition: reference('loop') })
    const latest = taskNodeRun('loop', 5)
    latest.nodeType = { oneofKind: 'task', task: {} }
    expect(guids(spec, [taskNodeRun('loop', 1), latest])).toEqual([])
  })
})

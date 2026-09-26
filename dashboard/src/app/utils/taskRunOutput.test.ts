import { TaskAttempt } from 'littlehorse-client/proto'
import { buildNodeOutputValuesFromNodeRuns, taskAttemptOutput } from './taskRunOutput'

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

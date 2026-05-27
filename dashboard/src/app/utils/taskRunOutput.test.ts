import { TaskAttempt } from 'littlehorse-client/proto'
import { buildNodeOutputValuesFromNodeRuns, taskAttemptOutput } from './taskRunOutput'

describe('taskAttemptOutput', () => {
  it('returns the latest successful output', () => {
    const attempts: TaskAttempt[] = [
      {
        result: { $case: 'error', value: { message: 'fail', type: 0 } },
        status: 0,
        taskWorkerId: '',
        maskedValue: false,
      },
      {
        result: { $case: 'output', value: { value: { $case: 'str', value: 'hello there, Obi-Wan' } } },
        status: 0,
        taskWorkerId: '',
        maskedValue: false,
      },
    ]
    expect(taskAttemptOutput(attempts)?.value?.$case).toBe('str')
    expect(taskAttemptOutput(attempts)?.value?.value).toBe('hello there, Obi-Wan')
  })
})

describe('buildNodeOutputValuesFromNodeRuns', () => {
  it('maps node names to task outputs', () => {
    const values = buildNodeOutputValuesFromNodeRuns(
      [
        {
          nodeName: '1-greet-TASK',
          nodeType: {
            $case: 'task',
            value: { taskRunId: { wfRunId: { id: 'x' }, taskGuid: '0-1' } },
          },
        } as never,
      ],
      new Map([
        [
          '0-1',
          {
            attempts: [
              {
                result: { $case: 'output', value: { value: { $case: 'str', value: 'hello there, Obi-Wan' } } },
                status: 0,
                taskWorkerId: '',
                maskedValue: false,
              },
            ],
          } as never,
        ],
      ])
    )
    expect(values['1-greet-TASK']?.value?.value).toBe('hello there, Obi-Wan')
  })
})

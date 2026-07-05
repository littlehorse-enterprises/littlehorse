import { useWhoAmI } from '@/contexts/WhoAmIContext'
import '@testing-library/jest-dom'
import { render, screen } from '@testing-library/react'
import { TaskRun, TaskStatus, Timestamp } from 'littlehorse-client/proto'
import { utcToLocalDateTime } from '@/app/utils'
import useSWR from 'swr'
import { TaskNodeRun } from '../TaskNodeRun'

/**
 * @jest-environment jsdom
 */

// Mocks must be set up before importing the component under test
jest.mock('swr', () => ({ __esModule: true, default: jest.fn() }))
jest.mock('@/contexts/WhoAmIContext', () => ({ useWhoAmI: jest.fn() }))
jest.mock('../../../NodeTypes/Task/getTaskRun', () => ({ getTaskRun: jest.fn() }))
jest.mock('../../../NodeTypes/Task/getCheckpoints', () => ({ getCheckpoints: jest.fn() }))

jest.mock('../../Components/NodeVariable', () => ({
  NodeVariable: ({ label, text, type }: any) => {
    const { utcToLocalDateTime } = require('@/app/utils')
    const display = type === 'date' ? utcToLocalDateTime(text) : String(text ?? '')
    return <div data-testid={`node-variable-${label}`}>{`${label}${display}${type ? `|${type}` : ''}`}</div>
  },
}))
jest.mock('../../Components', () => ({
  InputVariables: ({ variables }: any) => <div data-testid="input-variables">{JSON.stringify(variables)}</div>,
}))
jest.mock('../../Components/Attempts', () => ({
  Attempts: ({ attempts, attemptIndex }: any) => (
    <div data-testid="attempts">{`attempts:${attempts?.length ?? 0}:index:${attemptIndex}`}</div>
  ),
}))
jest.mock('../../Components/NodeStatus', () => ({
  NodeStatus: ({ status, type }: any) => {
    const { TaskStatus, LHStatus } = require('littlehorse-client/proto')
    const name = type === 'task' ? TaskStatus[status] : LHStatus[status]
    return <div data-testid="node-status">{`${name}:${type}`}</div>
  },
}))

// Now import the component under test

const useSWRMock = useSWR as jest.MockedFunction<typeof useSWR>
const useWhoAmIMock = useWhoAmI as jest.MockedFunction<typeof useWhoAmI>

describe('TaskNodeRun', () => {
  beforeEach(() => {
    jest.clearAllMocks()
    useWhoAmIMock.mockReturnValue({ tenantId: 'tenant-1' } as any)
  })

  it('renders node variables, status, attempts and input variables when nodeTask is available', () => {
    const nodeTask: TaskRun = {
      id: {
        wfRunId: {
          id: 'c7a5dd72910f4a77b1c1fc0f69546c40',
        },
        taskGuid: '0-2',
      },
      taskDefId: {
        name: 'greet',
      },
      attempts: [
        {
          logOutput: {
            value: {
              oneofKind: 'str',
              str: '',
            },
          },
          scheduleTime: Timestamp.fromDate(new Date('2025-12-02T16:17:00.649Z')),
          startTime: Timestamp.fromDate(new Date('2025-12-02T16:17:00.654Z')),
          endTime: Timestamp.fromDate(new Date('2025-12-02T16:17:00.677Z')),
          taskWorkerId: 'worker-d2ad24c018e949e5a06ee1484e207d0a',
          taskWorkerVersion: '',
          status: TaskStatus.TASK_SUCCESS,
          result: {
            oneofKind: 'output',
            output: {
              value: {
                oneofKind: 'str',
                str: 'hello there, hi from parent',
              },
            },
          },
          maskedValue: false,
        },
      ],
      inputVariables: [
        {
          varName: 'arg0',
          value: {
            value: {
              oneofKind: 'str',
              str: 'hi from parent',
            },
          },
          masked: false,
        },
      ],
      source: {
        taskRunSource: {
          oneofKind: 'taskNode',
          taskNode: {
            nodeRunId: {
              wfRunId: {
                id: 'c7a5dd72910f4a77b1c1fc0f69546c40',
              },
              threadRunNumber: 0,
              position: 2,
            },
          },
        },
      },
      scheduledAt: Timestamp.fromDate(new Date('2025-12-02T16:17:00.649Z')),
      status: TaskStatus.TASK_SUCCESS,
      timeoutSeconds: 60,
      totalAttempts: 1,
      totalCheckpoints: 0,
    }

    useSWRMock.mockReturnValueOnce({ data: nodeTask } as any).mockReturnValueOnce({ data: [] } as any)

    render(<TaskNodeRun node={{ taskRunId: { taskGuid: 'tg-1' } } as any} />)

    expect(screen.getByTestId('node-status')).toHaveTextContent('TASK_SUCCESS:task')
    expect(screen.getByTestId('node-variable-Node Type:')).toHaveTextContent('Node Type:Task')
    expect(screen.getByTestId('node-variable-taskGuid:')).toHaveTextContent('taskGuid:tg-1')
    expect(screen.getByTestId('node-variable-TaskDefId:')).toHaveTextContent('TaskDefId:greet')
    expect(screen.getByTestId('node-variable-position:')).toHaveTextContent('position:2')
    expect(screen.getByTestId('node-variable-threadRunNumber:')).toHaveTextContent('threadRunNumber:0')
    expect(screen.getByTestId('node-variable-wfRunId:')).toHaveTextContent('wfRunId:c7a5dd72910f4a77b1c1fc0f69546c40')
    expect(screen.getByTestId('node-variable-scheduledAt:')).toHaveTextContent(
      `scheduledAt:${utcToLocalDateTime(nodeTask.scheduledAt)}`
    )
    expect(screen.getByTestId('node-variable-timeoutSeconds:')).toHaveTextContent('timeoutSeconds:60')
    expect(screen.getByTestId('node-variable-totalCheckpoints:')).toHaveTextContent('totalCheckpoints:0')

    // Attempts and InputVariables rendered
    expect(screen.getByTestId('attempts')).toHaveTextContent('attempts:1:index:0')
    expect(screen.getByTestId('input-variables')).toHaveTextContent(JSON.stringify(nodeTask.inputVariables))
  })
})

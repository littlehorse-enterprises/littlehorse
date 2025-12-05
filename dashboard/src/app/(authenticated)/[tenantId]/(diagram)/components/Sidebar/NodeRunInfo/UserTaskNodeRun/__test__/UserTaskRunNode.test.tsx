import React from 'react'
import { render, screen } from '@testing-library/react'
import '@testing-library/jest-dom'
import useSWR, { MutatorOptions } from 'swr'
import { ContextProps, useWhoAmI } from '@/contexts/WhoAmIContext'
import { UserTaskNodeRun } from '../UserTaskNodeRun'
import {
  UserTaskEvent_UTETaskExecuted,
  UserTaskNodeRun as UserTaskNodeRunProto,
  UserTaskRun,
  UserTaskRunStatus,
  VariableValue,
} from 'littlehorse-client/proto'

jest.mock('@/contexts/WhoAmIContext', () => {
  return {
    useWhoAmI: jest.fn(),
  }
})

jest.mock('swr', () => {
  return {
    __esModule: true,
    default: jest.fn(),
  }
})

jest.mock('../../../../NodeTypes/UserTask/getUserTaskRun', () => {
  return {
    getUserTaskRun: jest.fn(),
  }
})

jest.mock('../../../Components/NodeVariable', () => {
  return {
    NodeVariable: ({ label, text }: { label: string; text: string }) =>
      React.createElement('div', { 'data-testid': `nodevar-${label}` }, `${label} ${text}`),
  }
})

jest.mock('../Events', () => {
  return {
    Events: () => React.createElement('div', {}, 'EventsMock'),
  }
})

jest.mock('../../../Components/Results', () => {
  return {
    Results: ({ variables }: { variables: [string, VariableValue][]; classTitle: string }) =>
      React.createElement('div', { 'data-testid': 'results-mock' }, `Results: ${JSON.stringify(variables)}`),
  }
})

describe('UserTaskRunNode', () => {
  const mockUseWhoAmI = useWhoAmI as jest.MockedFunction<typeof useWhoAmI>
  const mockUseSWR = useSWR as jest.MockedFunction<typeof useSWR>

  beforeEach(() => {
    jest.resetAllMocks()
    mockUseWhoAmI.mockReturnValue({ tenantId: 'tenant-1' } as ContextProps)
  })

  it('renders detailed fields, results and events when nodeTask data exists', () => {
    const executedEvent: UserTaskEvent_UTETaskExecuted = {
      taskRun: {
        wfRunId: {
          id: '23123',
        },
        taskGuid: 'id del task',
      },
    }
    const nodeTask: UserTaskRun = {
      userTaskDefId: {
        name: 'def-name',
        version: 0,
      },
      userGroup: 'group-1',
      userId: 'user-1',
      notes: 'some notes',
      scheduledTime: '2025-01-01T00:00:00Z',
      nodeRunId: {
        position: 3,
        threadRunNumber: 2,
        wfRunId: undefined,
      },
      epoch: 42,
      results: {
        requestedItem: {
          value: {
            $case: 'str',
            value: 'testing',
          },
        },
        justification: {
          value: {
            $case: 'str',
            value: 'as',
          },
        },
      },
      events: [{ time: new Date().toDateString(), event: { $case: 'taskExecuted', value: executedEvent } }],

      id: undefined,
      status: UserTaskRunStatus.UNASSIGNED,
    }

    mockUseSWR.mockReturnValue({
      data: nodeTask,
      error: undefined,
      mutate: function <MutationData = unknown>(
        data?: unknown,
        opts?: boolean | MutatorOptions<unknown, MutationData> | undefined
      ): Promise<unknown> {
        throw new Error('Function not implemented.')
      },
      isValidating: false,
      isLoading: false,
    })

    const node = {
      userTaskRunId: {
        wfRunId: { id: 'wf-2' },
        userTaskGuid: 'guid-2',
      },
    } as UserTaskNodeRunProto

    render(<UserTaskNodeRun node={node} />)

    expect(screen.getByTestId('nodevar-user_task_def_id:')).toHaveTextContent('user_task_def_id: def-name')
    expect(screen.getByTestId('nodevar-user_group:')).toHaveTextContent('user_group: group-1')
    expect(screen.getByTestId('nodevar-user_id:')).toHaveTextContent('user_id: user-1')
    expect(screen.getByTestId('nodevar-notes:')).toHaveTextContent('notes: some notes')
    expect(screen.getByTestId('nodevar-scheduled_time:')).toHaveTextContent('scheduled_time: 2025-01-01T00:00:00Z')
    expect(screen.getByTestId('nodevar-position:')).toHaveTextContent('position: 3')
    expect(screen.getByTestId('nodevar-threadRunNumber:')).toHaveTextContent('threadRunNumber: 2')
    expect(screen.getByTestId('nodevar-epoch:')).toHaveTextContent('epoch: 42')

    // Results and Events mocks
    expect(screen.getByTestId('results-mock')).toHaveTextContent('Results:')
    expect(screen.getByText('EventsMock')).toBeInTheDocument()
  })
})

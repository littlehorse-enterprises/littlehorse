import React from 'react'
import { render, screen } from '@testing-library/react'
import '@testing-library/jest-dom'
import { ThrowEventNodeRun } from '../ThrowEventNodeRun'
import { ThrowEventNodeRun as ThrowEventNodeRunProto, WorkflowEvent } from 'littlehorse-client/proto'

import useSWR, { MutatorOptions } from 'swr'
import { getWorkflowEvent } from '../../../NodeTypes/ThrowEvent/getWorkflowEvent'
import { ContextProps, useWhoAmI } from '@/contexts/WhoAmIContext'

jest.mock('swr', () => {
  return {
    __esModule: true,
    default: jest.fn(),
  }
})
jest.mock('@/contexts/WhoAmIContext', () => {
  return {
    useWhoAmI: jest.fn(),
  }
})

jest.mock('../../../NodeTypes/ThrowEvent/getWorkflowEvent', () => {
  return {
    getWorkflowEvent: jest.fn(),
  }
})
jest.mock('../../Components/NodeVariable', () => ({
  NodeVariable: ({ label, text }: { label: string; text: any }) => (
    <div data-testid={`nodevar-${label}`}>
      {label}:{String(text)}
    </div>
  ),
}))

describe('ThrowEventNodeRun', () => {
  const mockUseSWR = useSWR as jest.MockedFunction<typeof useSWR>
  const mockUseWhoAmI = useWhoAmI as jest.MockedFunction<typeof useWhoAmI>
  beforeEach(() => {
    jest.resetAllMocks()
    mockUseWhoAmI.mockReturnValue({ tenantId: 'tenant-1' } as ContextProps)
  })

  it('renders content area when workflowEvent content is present including variable type and value', () => {
    // Prepare a workflowEvent with content
    const workflowEvent: WorkflowEvent = {
      id: {
        wfRunId: {
          id: '3d444eb1635741a9a293f3446139ae20',
        },
        workflowEventDefId: {
          name: 'sleep-done',
        },
        number: 0,
      },
      content: {
        value: {
          $case: 'str',
          value: 'hello there!',
        },
      },
      createdAt: '2025-12-01T15:28:54.850Z',
      nodeRunId: {
        wfRunId: {
          id: '3d444eb1635741a9a293f3446139ae20',
        },
        threadRunNumber: 0,
        position: 2,
      },
    }

    mockUseSWR.mockReturnValue({
      data: workflowEvent,
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
      workflowEventId: {
        wfRunId: { id: 'run-id-2' },
        workflowEventDefId: { name: 'event-def-name-2' },
      },
    }

    render(<ThrowEventNodeRun node={node as ThrowEventNodeRunProto} />)

    expect(screen.getByTestId('nodevar-Node Type')).toHaveTextContent('Node Type:Throw event')
    expect(screen.getByTestId('nodevar-workflowEventId')).toHaveTextContent('workflowEventId:run-id-2')

    expect(screen.getByText('content:')).toBeInTheDocument()

    expect(screen.getByText('hello there!')).toBeInTheDocument()
    expect(screen.getByText('str')).toBeInTheDocument()
  })
})

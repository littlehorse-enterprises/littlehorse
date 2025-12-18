import React from 'react'
import { render, screen } from '@testing-library/react'
import { ExternalEventNodeRun as ExternalEventNodeRunProto } from 'littlehorse-client/proto'
import useSWR from 'swr'
import { ExternalEventNodeRun } from '../ExternalEventNodeRun'

jest.mock('swr', () => {
  return {
    __esModule: true,
    default: jest.fn(),
  }
})
jest.mock('../../../../NodeTypes/ExternalEvent/actions', () => {
  return {
    getExternalEvent: jest.fn(),
  }
})
jest.mock('@/app/(authenticated)/[tenantId]/externalEventDef/[name]/getExternalEventDef', () => {
  return {
    getExternalEventDef: jest.fn(),
  }
})
jest.mock('@/contexts/WhoAmIContext', () => ({ useWhoAmI: () => ({ tenantId: 'tenant1' }) }))

jest.mock('../ExternalEvent', () => ({
  ExternalEvent: () => <div data-testid="external-event">ExternalEventMock</div>,
}))
jest.mock('../ExternalEventDef', () => ({
  ExternalEventDef: () => <div data-testid="external-event-def">ExternalEventDefMock</div>,
}))

describe('ExternalEventNodeRun component', () => {
  const mockUseSWR = useSWR as jest.Mock
  afterEach(() => {
    jest.clearAllMocks()
  })

  it('renders basic fields including eventTime and correlationKey when provided', () => {
    const externalEventTest: ExternalEventNodeRunProto = {
      externalEventDefId: undefined,
      externalEventId: undefined,
      timedOut: false,
      maskCorrelationKey: false,
      eventTime: '2023-01-01T00:00:00Z',
      correlationKey: 'abc123',
    }
    mockUseSWR.mockReturnValue({
      data: undefined,
    })
    render(<ExternalEventNodeRun node={externalEventTest} />)

    expect(screen.getByText(/External event/i)).toBeInTheDocument()
    expect(screen.getByText(/eventTime:/i)).toBeInTheDocument()
    expect(screen.getByText(/2023-01-01T00:00:00Z/)).toBeInTheDocument()
    expect(screen.getByText(/abc123/)).toBeInTheDocument()
  })

  it('renders ExternalEventDef child components when SWR provides data', () => {
    mockUseSWR
      .mockImplementationOnce(() => ({ data: undefined }))
      .mockImplementationOnce(() => ({ data: { name: 'def1' } }))

    const externalEventTest: ExternalEventNodeRunProto = {
      externalEventDefId: { name: 'def1' },
      externalEventId: undefined,
      timedOut: false,
      maskCorrelationKey: false,
    }

    render(<ExternalEventNodeRun node={externalEventTest} />)

    expect(screen.getByTestId('external-event-def')).toBeInTheDocument()
    expect(screen.queryByTestId('external-event')).not.toBeInTheDocument()
  })
  it('renders ExternalEvent and ExternalEventDef child components when SWR provides data', () => {
    mockUseSWR
      .mockImplementationOnce(() => ({ data: { id: 'ev1' } }))
      .mockImplementationOnce(() => ({ data: { name: 'def1' } }))

    const externalEventTest: ExternalEventNodeRunProto = {
      externalEventDefId: { name: 'def1' },
      externalEventId: {
        wfRunId: {
          id: '51b58511bbce416d8805c5a6b6c173ad',
        },
        externalEventDefId: {
          name: 'name-event',
        },
        guid: '4c6a5e3b70e3412b8e891eb4d1422976',
      },
      timedOut: false,
      maskCorrelationKey: false,
    }

    render(<ExternalEventNodeRun node={externalEventTest} />)

    expect(screen.getByTestId('external-event')).toBeInTheDocument()
    expect(screen.getByTestId('external-event-def')).toBeInTheDocument()
  })
})

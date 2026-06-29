import React from 'react'
import { render, screen, fireEvent } from '@testing-library/react'
import '@testing-library/jest-dom'
import { WaitForThreadsNodeRun } from '../WaitForThreadNodeRun'
import { LHStatus, Timestamp, WaitForThreadsRun, WaitForThreadsRun_WaitingThreadStatus } from 'littlehorse-client/proto'
import { utcToLocalDateTime } from '@/app/utils'

jest.mock('@/components/ui/accordion', () => {
  const AccordionContext = React.createContext<{ open: boolean; toggle: () => void } | null>(null)

  const Accordion: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    return <div data-testid="mock-accordion">{children}</div>
  }

  const AccordionItem: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const [open, setOpen] = React.useState(false)
    const toggle = () => setOpen(s => !s)
    return (
      <AccordionContext.Provider value={{ open, toggle }}>
        <div data-testid="mock-accordion-item">{children}</div>
      </AccordionContext.Provider>
    )
  }

  const AccordionTrigger: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const ctx = React.useContext(AccordionContext)
    return (
      <button data-testid="mock-accordion-trigger" onClick={ctx?.toggle}>
        {children}
      </button>
    )
  }

  const AccordionContent: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const ctx = React.useContext(AccordionContext)
    return ctx?.open ? <div data-testid="mock-accordion-content">{children}</div> : null
  }

  return { Accordion, AccordionItem, AccordionTrigger, AccordionContent }
})

jest.mock('../../Components/NodeStatus', () => {
  return {
    NodeStatus: ({ status }: { status: number }) => {
      const { LHStatus } = require('littlehorse-client/proto')
      return <div data-testid="node-status">status:{LHStatus[status]}</div>
    },
  }
})

jest.mock('../../Components/NodeVariable', () => {
  const { utcToLocalDateTime } = jest.requireActual('@/app/utils')
  return {
    NodeVariable: ({ label, text, type }: { label: string; text: unknown; type?: string }) => (
      <div data-testid="node-variable">
        {label}
        {type === 'date' ? utcToLocalDateTime(text) : String(text)}
      </div>
    ),
  }
})

describe('WaitForThreadsNodeRun', () => {
  const node: WaitForThreadsRun = {
    threads: [
      {
        threadRunNumber: 12,
        threadStatus: LHStatus.RUNNING,
        threadEndTime: Timestamp.fromDate(new Date('2023-01-01T00:00:00Z')),
        waitingStatus: WaitForThreadsRun_WaitingThreadStatus.THREAD_IN_PROGRESS,
        failureHandlerThreadRunId: 123,
      },
      {
        threadRunNumber: 13,
        threadStatus: LHStatus.ERROR,
        threadEndTime: undefined,
        waitingStatus: WaitForThreadsRun_WaitingThreadStatus.THREAD_COMPLETED_OR_FAILURE_HANDLED,
        failureHandlerThreadRunId: undefined,
      },
    ],
  }

  test('renders one trigger per thread', () => {
    render(<WaitForThreadsNodeRun node={node} />)

    expect(screen.getAllByTestId('mock-accordion-trigger')).toHaveLength(node.threads.length)
    expect(screen.getByText(12)).toBeInTheDocument()
    expect(screen.getByText(13)).toBeInTheDocument()
  })

  test('shows thread details when accordion is toggled', () => {
    render(<WaitForThreadsNodeRun node={node} />)

    const triggers = screen.getAllByTestId('mock-accordion-trigger')
    // Open first thread
    fireEvent.click(triggers[0])

    // Content should appear
    expect(screen.getByTestId('mock-accordion-content')).toBeInTheDocument()

    // NodeStatus should reflect threadStatus
    expect(screen.getByTestId('node-status')).toHaveTextContent('status:RUNNING')

    // NodeVariable entries should include expected labels and values
    expect(screen.getAllByTestId('node-variable').map(el => el.textContent)).toEqual(
      expect.arrayContaining([
        'Node Type:Thread',
        `threadEndTime:${utcToLocalDateTime(node.threads[0].threadEndTime)}`,
        'waitingStatus:THREAD_IN_PROGRESS',
        'failureHandlerThreadRunId:123',
      ])
    )
  })

  test('handles null/undefined values gracefully', () => {
    render(<WaitForThreadsNodeRun node={node} />)

    const triggers = screen.getAllByTestId('mock-accordion-trigger')
    // Open second thread which has undefined values
    fireEvent.click(triggers[1])

    expect(screen.getByTestId('mock-accordion-content')).toBeInTheDocument()
    // failureHandlerThreadRunId null should be rendered as "undefined"
    expect(screen.getAllByTestId('node-variable')).toEqual(
      expect.arrayContaining([expect.objectContaining({ textContent: 'failureHandlerThreadRunId:undefined' })])
    )
  })
})

import React from 'react'
import { render, screen } from '@testing-library/react'
import { SavedEvent } from '../SavedEvent'
import { UserTaskEvent_UTESaved, VariableValue } from 'littlehorse-client/proto'

jest.mock('@/app/utils', () => ({
  formatDateReadable: jest.fn(() => 'FORMATTED_DATE'),
}))

jest.mock('../../../Components/Results', () => ({
  Results: ({ variables, classTitle }: { variables: [string, VariableValue][]; classTitle: string }) => (
    <div data-testid="results">
      {JSON.stringify(variables)}
      {classTitle}
    </div>
  ),
}))

describe('SavedEvent', () => {
  it('renders saved label, formatted time and user id when time is provided', () => {
    const event: UserTaskEvent_UTESaved = { userId: 'alice', results: {} }
    render(<SavedEvent event={event} time="2020-01-01T00:00:00Z" />)

    expect(screen.getByText('FORMATTED_DATE')).toBeInTheDocument()
    expect(screen.getByText(/It has saved by alice/)).toBeInTheDocument()
  })

  it('renders Results when event.results has entries', () => {
    const savedEvent: UserTaskEvent_UTESaved = {
      userId: '123123123',
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
    }
    render(<SavedEvent event={savedEvent} />)

    const results = screen.getByTestId('results')
    expect(results).toBeInTheDocument()
    expect(results).toHaveTextContent(JSON.stringify(Object.entries(savedEvent.results)))
  })

  it('does not render Results when event.results is empty', () => {
    const event: UserTaskEvent_UTESaved = { userId: 'carol', results: {} }
    render(<SavedEvent event={event} />)

    expect(screen.queryByTestId('results')).toBeNull()
  })
})

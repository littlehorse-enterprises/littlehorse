import React from 'react'
import { render, screen } from '@testing-library/react'
import { ExternalEvent } from '../ExternalEvent'
import { ExternalEvent as ExternalEventProto } from 'littlehorse-client/proto'

describe('ExternalEvent component', () => {
  const externalEventTest: ExternalEventProto = {
    id: {
      externalEventDefId: { name: 'UserSignup' },
      guid: 'evt-12345',
      wfRunId: {
        id: '51b58511bbce416d8805c5a6b6c173ad',
      },
    },
    createdAt: new Date().toISOString(),
    content: {
      value: {
        $case: 'str',
        value: 'Obi-Wan',
      },
    },
    claimed: false,
  }

  it('renders ExternalEvent component with correct data', () => {
    render(<ExternalEvent event={externalEventTest} />)

    expect(screen.getByText('ExternalEvent')).toBeInTheDocument()
    expect(screen.getByText('externalEventId:')).toBeInTheDocument()
    expect(screen.getByText('51b58511bbce416d8805c5a6b6c173ad')).toBeInTheDocument()
    expect(screen.getByText('UserSignup')).toBeInTheDocument()
    expect(screen.getByText('str')).toBeInTheDocument()
    expect(screen.getByText('guid:')).toBeInTheDocument()
    expect(screen.getByText('evt-12345')).toBeInTheDocument()
    expect(screen.getByText('createdAt:')).toBeInTheDocument()
    expect(screen.getByText('claimed:')).toBeInTheDocument()
    expect(screen.getByText('No')).toBeInTheDocument()
  })
})

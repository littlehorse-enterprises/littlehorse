import React from 'react'
import { render, screen } from '@testing-library/react'
import { ExternalEventDef as ExternalEventDefProto } from 'littlehorse-client/proto'
import { ExternalEventDef } from '../ExternalEventDef'

describe('ExternalEventDef component', () => {
  const externalEventTest: ExternalEventDefProto = {
    id: {
      name: 'name-event',
    },
    createdAt: '2025-11-28T14:11:24.584Z',
    retentionPolicy: { extEvtGcPolicy: { $case: 'secondsAfterPut', value: 12 } },
  }

  it('renders ExternalEventDef component with correct data', () => {
    render(<ExternalEventDef event={externalEventTest} />)
    expect(screen.getByText('ExternalEventDef')).toBeInTheDocument()
    expect(screen.getByText('ExternalEventDefId:')).toBeInTheDocument()
    expect(screen.getByText('name-event')).toBeInTheDocument()
    expect(screen.getByText('createdAt:')).toBeInTheDocument()
    expect(screen.getByText('retentionPolicy:')).toBeInTheDocument()
    expect(screen.getByText('12')).toBeInTheDocument()
  })
})

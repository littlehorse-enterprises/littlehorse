import React from 'react'
import { render } from '@testing-library/react'
import { DoneEvent } from '../DoneEvent'

describe('DoneEvent', () => {
  it('renders without crashing given time', () => {
    const { container } = render(<DoneEvent time="2025-01-01T12:00:00Z" />)
    expect(container).toBeInTheDocument()
  })
})

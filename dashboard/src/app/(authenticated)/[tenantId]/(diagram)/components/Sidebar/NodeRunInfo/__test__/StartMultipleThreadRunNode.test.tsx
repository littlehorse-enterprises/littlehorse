import React from 'react'
import { render, screen } from '@testing-library/react'
import { StartMultipleThreadNodeRun } from '../StartMultipleThreadNodeRun'
import { StartMultipleThreadsRun } from 'littlehorse-client/proto'

describe('StartMultipleThreadRunNode', () => {
  test('renders node type label and threadSpecName text', () => {
    const node:StartMultipleThreadsRun = {
      threadSpecName: 'my-spec',
      childThreadIds: [12, 123],
    }

    render(<StartMultipleThreadNodeRun node={node} />)

    expect(screen.getByText(/Start multiple threads/i)).toBeInTheDocument()
    expect(screen.getByText('my-spec')).toBeInTheDocument()
  })

  test('renders each childThreadId as a blue text element', () => {
    const node:StartMultipleThreadsRun   = {
      threadSpecName: 'spec-1',
      childThreadIds: [12, 123, 456],
    }

    const { container } = render(<StartMultipleThreadNodeRun node={node} />)

    const childThreadIds = Array.from(container.querySelectorAll('.ml-1.text-blue-500'))
    expect(childThreadIds).toHaveLength(3)
    expect(childThreadIds.map(el => el.textContent)).toEqual(['12', '123', '456'])
  })

  test('handles empty childThreadIds without errors', () => {
    const node:StartMultipleThreadsRun = {
      threadSpecName: 'empty-spec',
      childThreadIds: [],
    }

    const { container } = render(<StartMultipleThreadNodeRun node={node} />)

    expect(screen.getByText('empty-spec')).toBeInTheDocument()
    const childThreadIds = container.querySelectorAll('.ml-1.text-blue-500')
    expect(childThreadIds.length).toBe(0)
  })
})

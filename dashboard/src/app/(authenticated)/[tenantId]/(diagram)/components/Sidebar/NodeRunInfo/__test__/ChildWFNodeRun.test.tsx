import React from 'react'
import { RunChildWfNodeRun as RunChildWfNodeRunProto } from 'littlehorse-client/proto'

import '@testing-library/jest-dom'
import { render, screen } from '@testing-library/react'

jest.mock('next/navigation', () => ({
  useParams: () => ({ tenantId: 'tenant-1' }),
}))

import { ChildWFNodeRun } from '../ChildWFNodeRun'

describe('ChildWFNodeRun', () => {
  test('renders Node Type label and Child Workflow text and builds correct link when ids present', () => {
    const node = {
      childWfRunId: {
        id: 'child-1',
        parentWfRunId: { id: 'parent-1' },
      },
    } as RunChildWfNodeRunProto

    render(<ChildWFNodeRun node={node} />)

    expect(screen.getByText('Node Type')).toBeInTheDocument()
    expect(screen.getByText('Child Workflow')).toBeInTheDocument()

    const link = screen.getByRole('link', { name: 'child-1' })
    expect(link).toBeInTheDocument()
    expect(link.getAttribute('href')).toContain('/wfRun/parent-1/child-1')
  })
})

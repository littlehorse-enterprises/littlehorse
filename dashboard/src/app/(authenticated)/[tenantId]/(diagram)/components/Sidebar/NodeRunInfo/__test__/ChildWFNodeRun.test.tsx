import React from 'react'
import { RunChildWfNodeRun as RunChildWfNodeRunProto } from 'littlehorse-client/proto'

import '@testing-library/jest-dom'
import { render, screen } from '@testing-library/react'

jest.mock('next/navigation', () => ({
  useParams: () => ({ tenantId: 'tenant-1' }),
}))

jest.mock('../../Components/NodeVariable', () => ({
  NodeVariable: ({ label, text, type, link }: any) => (
    <div data-testid={`node-variable-${label}`}>
      {link ? <a href={link}>{text}</a> : `${label}${String(text ?? '')}${type ? `|${type}` : ''}`}
    </div>
  ),
}))
jest.mock('../../Components', () => ({
  InputVariables: ({ variables }: any) => <div data-testid="input-variables">{JSON.stringify(variables)}</div>,
}))

import { ChildWFNodeRun } from '../ChildWFNodeRun'

describe('ChildWFNodeRun', () => {
  test('renders Node Type label and Run Child Workflow text and builds correct link when ids present', () => {
    const node = {
      childWfRunId: {
        id: 'child-1',
        parentWfRunId: { id: 'parent-1' },
      },
    } as RunChildWfNodeRunProto

    render(<ChildWFNodeRun node={node} />)

    expect(screen.getByTestId('node-variable-Node Type')).toHaveTextContent(/Node Type/)
    expect(screen.getByTestId('node-variable-Node Type')).toHaveTextContent(/Run Child Workflow/)

    const link = screen.getByRole('link', { name: 'child-1' })
    expect(link).toBeInTheDocument()
    expect(link.getAttribute('href')).toContain('/wfRun/parent-1/child-1')
  })
})

import { render, screen } from '@testing-library/react'
import { ComponentProps } from 'react'
import {
  InlineWfSpec,
  LHStatus,
  ThreadRun,
  ThreadSpec,
  Timestamp,
  WfRun as WfRunProto,
  WfSpec,
} from 'littlehorse-client/proto'
import { Diagram } from '../../../components/Diagram'
import { WfRun } from './WfRun'

jest.mock('@/app/actions/getWfRun', () => ({ getWfRun: jest.fn() }))
jest.mock('swr', () => ({
  __esModule: true,
  default: (_key: unknown, _fetcher: unknown, options: { fallbackData: unknown }) => ({ data: options.fallbackData }),
}))
jest.mock('@/contexts/WhoAmIContext', () => ({ useWhoAmI: () => ({ tenantId: 'test-tenant' }) }))
jest.mock('../../../hooks/useModal', () => ({
  useModal: () => ({ setModal: jest.fn(), setShowModal: jest.fn() }),
}))
jest.mock('../../../components/Diagram', () => ({ Diagram: jest.fn(() => <div>Workflow graph</div>) }))
jest.mock('./Variables', () => ({ Variables: () => <div>Workflow variables</div> }))
jest.mock('./ChildWorkflows', () => ({ ChildWorkflows: () => <div>Child workflows</div> }))

const thread = ThreadRun.create({ number: 0, threadSpecName: 'main', errorMessage: 'Task failed' })
const threadSpecs = { main: ThreadSpec.create() }
const run = WfRunProto.create({
  id: { id: 'child', parentWfRunId: { id: 'parent' } },
  status: LHStatus.ERROR,
  startTime: Timestamp.fromDate(new Date('2026-10-07T12:00:00Z')),
  threadRuns: [thread],
})

beforeEach(() => jest.clearAllMocks())

it('shows inline run details and reuses the graph without registered WfSpec links', () => {
  const spec = InlineWfSpec.create({ id: run.id, threadSpecs, entrypointThreadName: 'main' })
  const wfRun = {
    ...WfRunProto.create({ ...run, wfSpecSource: { oneofKind: 'isInline', isInline: true } }),
    threadRuns: [{ ...thread, nodeRuns: [] }],
  }
  render(<WfRun wfRun={wfRun} wfSpec={spec} variables={[]} variablesTooLarge={false} />)

  expect(screen.getByRole('heading', { name: 'child' })).toBeInTheDocument()
  expect(screen.getByText('Inline workflow')).toBeInTheDocument()
  expect(screen.getByText('Status:')).toHaveTextContent('ERROR')
  expect(screen.getByText('Started:')).toBeInTheDocument()
  expect(screen.getByRole('button', { name: 'Task failed' })).toBeInTheDocument()
  expect(screen.getByRole('link', { name: 'parent' })).toHaveAttribute('href', '/test-tenant/wfRun/parent')
  expect(screen.queryByRole('link', { name: 'Go back to WfSpec' })).not.toBeInTheDocument()
  expect(screen.getByText('Workflow variables')).toBeInTheDocument()
  expect(screen.getByText('Child workflows')).toBeInTheDocument()
  const diagramProps: ComponentProps<typeof Diagram> = jest.mocked(Diagram).mock.calls[0][0]
  expect(diagramProps.spec).toEqual(spec)
  expect(diagramProps.wfRun).toEqual(wfRun)
  expect(diagramProps.definitionId).toBe('inline/parent/child')
})

it('keeps registered workflow names, versions, and navigation links', () => {
  const id = { name: 'example', majorVersion: 1, revision: 2 }
  const spec = WfSpec.create({ id, threadSpecs, entrypointThreadName: 'main' })
  const wfRun = {
    ...WfRunProto.create({ ...run, wfSpecSource: { oneofKind: 'wfSpecId', wfSpecId: id } }),
    threadRuns: [{ ...thread, nodeRuns: [] }],
  }
  render(<WfRun wfRun={wfRun} wfSpec={spec} variables={[]} variablesTooLarge={false} />)

  expect(screen.getByRole('link', { name: 'example 1.2' })).toHaveAttribute('href', '/test-tenant/wfSpec/example/1/2')
  expect(screen.getByRole('link', { name: 'Go back to WfSpec' })).toHaveAttribute(
    'href',
    '/test-tenant/wfSpec/example/1/2'
  )
  expect(screen.queryByText('Inline workflow')).not.toBeInTheDocument()
  expect(jest.mocked(Diagram).mock.calls[0][0].definitionId).toBe('/wfSpec/example/1/2')
})

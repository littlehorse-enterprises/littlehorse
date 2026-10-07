import { lhClient } from '@/app/lhClient'
import { InlineWfSpec, NodeRun, ThreadRun, ThreadSpec, Variable, WfRun, WfSpec } from 'littlehorse-client/proto'
import { getInheritedVariables } from './getInheritedVariables'
import { getWfRun } from './getWfRun'

jest.mock('@/app/lhClient', () => ({ lhClient: jest.fn() }))
jest.mock('./getInheritedVariables', () => ({ getInheritedVariables: jest.fn() }))

const client = {
  getWfRun: jest.fn(),
  getWfSpec: jest.fn(),
  getInlineWfSpec: jest.fn(),
  listNodeRuns: jest.fn(),
  listVariables: jest.fn(),
}
const wfRunId = { id: 'child', parentWfRunId: { id: 'parent' } }
const specId = { name: 'example', majorVersion: 1, revision: 2 }
const threadSpecs = { main: ThreadSpec.create() }

beforeEach(() => {
  jest.resetAllMocks()
  jest.mocked(lhClient).mockResolvedValue(client as unknown as Awaited<ReturnType<typeof lhClient>>)
  client.listNodeRuns.mockResolvedValue({ results: [] })
  client.listVariables.mockResolvedValue({ results: [] })
  jest.mocked(getInheritedVariables).mockResolvedValue({ variables: [], tooLarge: false })
})

it('loads the registered definition from the wfSpecSource oneof', async () => {
  const spec = WfSpec.create({ id: specId, threadSpecs, entrypointThreadName: 'main' })
  client.getWfRun.mockResolvedValue(
    WfRun.create({ id: wfRunId, wfSpecSource: { oneofKind: 'wfSpecId', wfSpecId: specId } })
  )
  client.getWfSpec.mockResolvedValue(spec)

  const result = await getWfRun({ wfRunId, tenantId: 'test-tenant' })

  expect(lhClient).toHaveBeenCalledWith({ tenantId: 'test-tenant' })
  expect(client.getWfSpec).toHaveBeenCalledWith(specId)
  expect(client.getInlineWfSpec).not.toHaveBeenCalled()
  expect(result.wfSpec).toEqual(spec)
})

it('loads an inline snapshot by the full WfRun ID and retains nodes and variables', async () => {
  const spec = InlineWfSpec.create({ id: wfRunId, threadSpecs, entrypointThreadName: 'main' })
  const node = NodeRun.create({ id: { wfRunId, threadRunNumber: 0, position: 1 }, threadSpecName: 'main' })
  const variable = Variable.create({ id: { wfRunId, threadRunNumber: 0, name: 'input' } })
  client.getWfRun.mockResolvedValue(
    WfRun.create({
      id: wfRunId,
      wfSpecSource: { oneofKind: 'isInline', isInline: true },
      threadRuns: [ThreadRun.create({ number: 0, threadSpecName: 'main' })],
    })
  )
  client.getInlineWfSpec.mockResolvedValue(spec)
  client.listNodeRuns.mockResolvedValue({ results: [node] })
  client.listVariables.mockResolvedValue({ results: [variable] })

  const result = await getWfRun({ wfRunId, tenantId: 'test-tenant' })

  expect(client.getInlineWfSpec).toHaveBeenCalledWith(wfRunId)
  expect(client.getWfSpec).not.toHaveBeenCalled()
  expect(result.wfSpec).toEqual(spec)
  expect(result.wfRun.threadRuns[0].nodeRuns).toEqual([node])
  expect(result.variables).toEqual([variable])
  expect(getInheritedVariables).toHaveBeenCalledWith(wfRunId, threadSpecs.main.variableDefs, 'test-tenant')
})

it('rejects a missing workflow source instead of requesting a registered spec', async () => {
  client.getWfRun.mockResolvedValue(WfRun.create({ id: wfRunId }))

  await expect(getWfRun({ wfRunId, tenantId: 'test-tenant' })).rejects.toThrow('workflow definition source')
  expect(client.getWfSpec).not.toHaveBeenCalled()
  expect(client.getInlineWfSpec).not.toHaveBeenCalled()
})

it('rejects an inline marker set to false', async () => {
  client.getWfRun.mockResolvedValue(
    WfRun.create({ id: wfRunId, wfSpecSource: { oneofKind: 'isInline', isInline: false } })
  )

  await expect(getWfRun({ wfRunId, tenantId: 'test-tenant' })).rejects.toThrow('workflow definition source')
  expect(client.getInlineWfSpec).not.toHaveBeenCalled()
})

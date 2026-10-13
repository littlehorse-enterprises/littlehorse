/** @jest-environment node */
import { lhClient } from '@/app/lhClient'
import { NodeRun, TaskRun, TaskRunId, ThreadRun, VariableAssignment, WfRun, WfSpec } from 'littlehorse-client/proto'
import { ClientError, Status } from 'nice-grpc-common'
import { getWfRun } from './getWfRun'
import { searchWfRun } from '../(authenticated)/[tenantId]/(diagram)/wfSpec/[...props]/actions/searchWfRun'

jest.mock('@/app/lhClient', () => ({ lhClient: jest.fn() }))
jest.mock('./getInheritedVariables', () => ({
  getInheritedVariables: jest.fn().mockResolvedValue({ variables: [], tooLarge: false }),
}))

const wfRunId = { id: 'run' }
const props = { wfRunId, tenantId: 'tenant' }
const client = {
  getWfRun: jest.fn(),
  getWfSpec: jest.fn(),
  listNodeRuns: jest.fn(),
  listVariables: jest.fn(),
  getTaskRun: jest.fn<Promise<TaskRun>, [TaskRunId]>(),
  searchWfRun: jest.fn(),
}

const taskRun = (id: TaskRunId): TaskRun =>
  TaskRun.create({
    id,
    attempts: [{ result: { oneofKind: 'output', output: { value: { oneofKind: 'str', str: id.taskGuid } } } }],
  })

const configureWorkflow = (referencedNames: string[], allNames = referencedNames) => {
  client.getWfRun.mockResolvedValue(
    WfRun.create({
      id: wfRunId,
      wfSpecId: { name: 'spec' },
      threadRuns: [ThreadRun.create({ number: 0, threadSpecName: 'main' })],
    })
  )
  client.getWfSpec.mockResolvedValue(
    WfSpec.create({
      entrypointThreadName: 'main',
      threadSpecs: {
        main: {
          nodes: {
            branch: {
              outgoingEdges: referencedNames.map(nodeName => ({
                edgeCondition: {
                  oneofKind: 'condition',
                  condition: VariableAssignment.create({
                    source: { oneofKind: 'nodeOutput', nodeOutput: { nodeName } },
                  }),
                },
              })),
            },
          },
        },
      },
    })
  )
  client.listNodeRuns.mockResolvedValue({
    results: allNames.map((nodeName, position) =>
      NodeRun.create({
        id: { wfRunId, threadRunNumber: 0, position },
        nodeName,
        threadSpecName: 'main',
        nodeType: { oneofKind: 'task', task: { taskRunId: { wfRunId, taskGuid: nodeName } } },
      })
    ),
  })
}

beforeEach(() => {
  jest.clearAllMocks()
  jest.mocked(lhClient).mockResolvedValue(client as unknown as Awaited<ReturnType<typeof lhClient>>)
  client.listVariables.mockResolvedValue({ results: [] })
  client.getTaskRun.mockImplementation(async id => taskRun(id))
  client.searchWfRun.mockResolvedValue({ results: [wfRunId] })
  configureWorkflow(['first', 'second'], ['first', 'second', 'unused'])
})

afterEach(() => jest.restoreAllMocks())

it.each([Status.NOT_FOUND, Status.UNAVAILABLE, Status.RESOURCE_EXHAUSTED])(
  'preserves the workflow and other outputs when a task fetch fails with status %s',
  async status => {
    const error = new ClientError('/GetTaskRun', status, 'Task output unavailable')
    const log = jest.spyOn(console, 'error').mockImplementation(() => {})
    client.getTaskRun.mockImplementation(async id => {
      if (id.taskGuid === 'first') throw error
      return taskRun(id)
    })

    const result = await getWfRun(props)
    expect(result.wfRun.id).toEqual(wfRunId)
    expect(result.wfRun.threadRuns[0].nodeRuns).toHaveLength(3)
    expect(result.wfRun.threadRuns[0].nodeOutputValues).toEqual({
      second: { value: { oneofKind: 'str', str: 'second' } },
    })
    expect(log).toHaveBeenCalledWith('Error getting task output for diagram', { wfRunId, taskGuid: 'first' }, error)
  }
)

it('fetches only outputs referenced by conditions', async () => {
  const result = await getWfRun(props)
  expect(client.getTaskRun.mock.calls.map(([id]) => id.taskGuid)).toEqual(['first', 'second'])
  expect(Object.keys(result.wfRun.threadRuns[0].nodeOutputValues!)).toEqual(['first', 'second'])
})

it('makes no task requests for a workflow without output conditions', async () => {
  configureWorkflow([], ['unused'])
  await getWfRun(props)
  expect(client.getTaskRun).not.toHaveBeenCalled()
})

it('skips task outputs for run-list searches even when conditions reference them', async () => {
  const result = await searchWfRun({
    tenantId: 'tenant',
    bookmarkAsString: undefined,
    wfSpecName: 'spec',
    variableFilters: [],
  })
  expect(result.results).toHaveLength(1)
  expect(result.results[0].wfRun.id).toEqual(wfRunId)
  expect(client.getTaskRun).not.toHaveBeenCalled()
})

it('still propagates failures fetching the workflow itself', async () => {
  const error = new ClientError('/GetWfRun', Status.NOT_FOUND, 'Workflow not found')
  client.getWfRun.mockRejectedValue(error)
  await expect(getWfRun(props)).rejects.toBe(error)
  expect(client.getTaskRun).not.toHaveBeenCalled()
})

it('bounds concurrent task fetches and processes every referenced output', async () => {
  configureWorkflow(Array.from({ length: 23 }, (_, index) => `task-${index}`))
  let inFlight = 0
  let maxInFlight = 0
  client.getTaskRun.mockImplementation(async id => {
    inFlight++
    maxInFlight = Math.max(maxInFlight, inFlight)
    await new Promise<void>(resolve => setImmediate(resolve))
    inFlight--
    return taskRun(id)
  })

  const result = await getWfRun(props)
  expect(maxInFlight).toBeGreaterThan(1)
  expect(maxInFlight).toBeLessThanOrEqual(10)
  expect(client.getTaskRun).toHaveBeenCalledTimes(23)
  expect(Object.keys(result.wfRun.threadRuns[0].nodeOutputValues!)).toHaveLength(23)
})

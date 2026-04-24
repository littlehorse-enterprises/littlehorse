import type { Client } from 'nice-grpc'
import { VariableMutationType } from '../../proto/common_wfspec'
import { LittleHorseDefinition, PutWfSpecRequest } from '../../proto/service'
import { Workflow } from '../workflow'

describe('WorkflowCompilation', () => {
  it('caches compile() result (entrypoint runs once)', () => {
    let runs = 0
    const wf = Workflow.newWorkflow('cached', () => {
      runs++
    })
    expect(wf.compile()).toBe(wf.compile())
    expect(runs).toBe(1)
  })

  it('registerWfSpec forwards compile() to putWfSpec', async () => {
    const wf = Workflow.newWorkflow('reg', thread => thread.complete())
    const putWfSpec = jest.fn().mockResolvedValue({})
    const client = { putWfSpec } as unknown as Client<typeof LittleHorseDefinition>
    await Workflow.registerWfSpec(wf, client)
    expect(putWfSpec).toHaveBeenCalledTimes(1)
    expect(putWfSpec).toHaveBeenCalledWith(wf.compile())
  })

  it('builds entrypoint through exit for an empty thread', () => {
    const wf = Workflow.newWorkflow('empty', () => {})
    const spec = wf.compile()
    expect(spec.name).toBe('empty')
    expect(spec.entrypointThreadName).toBe('entrypoint')
    const thread = spec.threadSpecs['entrypoint']
    expect(thread).toBeDefined()
    const nodes = thread!.nodes
    const names = Object.keys(nodes).sort()
    expect(names.length).toBe(2)
    expect(nodes[names[0]]!.node?.$case).toBe('entrypoint')
    expect(nodes[names[1]]!.node?.$case).toBe('exit')
  })

  it('matches proposal 017 example-order-flow shape', () => {
    const wf = Workflow.newWorkflow('example-order-flow', thread => {
      const orderId = thread.declareStr('order-id')
      const total = thread.declareDouble('total', 0)

      const fetch = thread.execute('fetch-order', orderId)
      total.assign(fetch.jsonPath('$.total'))

      const taxRate = thread.declareDouble('tax-rate', 0.08)
      const amountDue = total.add(total.multiply(taxRate))
      const charged = thread.execute('charge-card', amountDue, orderId).withRetries(3)

      thread.complete(charged)
    })

    const spec = wf.compile()
    expect(spec.name).toBe('example-order-flow')
    const thread = spec.threadSpecs['entrypoint']!
    expect(thread.variableDefs.length).toBe(3)

    const taskNodes = Object.values(thread.nodes).filter(n => n.node?.$case === 'task')
    expect(taskNodes.length).toBe(2)

    const exit = Object.values(thread.nodes).find(n => n.node?.$case === 'exit')
    expect(exit?.node?.$case).toBe('exit')
    if (exit?.node?.$case === 'exit') {
      expect(exit.node.value.result?.$case).toBe('returnContent')
    }

    const edgesWithMutations = Object.values(thread.nodes).flatMap(n => n.outgoingEdges)
    const assignTotal = edgesWithMutations
      .flatMap(e => e.variableMutations)
      .find(m => m.lhsName === 'total' && m.operation === VariableMutationType.ASSIGN)
    expect(assignTotal).toBeDefined()
  })

  it('supports task.withTimeout and withRetries', () => {
    const wf = Workflow.newWorkflow('parity', thread => {
      const x = thread.declareInt('x')
      thread.execute('t', x).withTimeout(120).withRetries(1)
      thread.complete()
    })
    const thread = wf.compile().threadSpecs['entrypoint']!
    const task = Object.values(thread.nodes).find(n => n.node?.$case === 'task')
    expect(task?.node?.$case).toBe('task')
    if (task?.node?.$case === 'task') {
      expect(task.node.value.timeoutSeconds).toBe(120)
      expect(task.node.value.retries).toBe(1)
    }
  })

  it('supports declareJsonObj and jsonPath on task arguments', () => {
    const wf = Workflow.newWorkflow('wf-json-path', thread => {
      const person = thread.declareJsonObj('person')
      thread.execute('greet', person.jsonPath('$.name'))
      thread.execute('describe-car', person.jsonPath('$.car'))
    })
    const spec = wf.compile()
    expect(spec.name).toBe('wf-json-path')
    const tasks = Object.values(spec.threadSpecs['entrypoint']!.nodes).filter(n => n.node?.$case === 'task')
    expect(tasks.length).toBe(2)
  })

  it('example-order-flow PutWfSpecRequest JSON snapshot', () => {
    const wf = Workflow.newWorkflow('example-order-flow', thread => {
      const orderId = thread.declareStr('order-id')
      const total = thread.declareDouble('total', 0)

      const fetch = thread.execute('fetch-order', orderId)
      total.assign(fetch.jsonPath('$.total'))

      const taxRate = thread.declareDouble('tax-rate', 0.08)
      const amountDue = total.add(total.multiply(taxRate))
      const charged = thread.execute('charge-card', amountDue, orderId).withRetries(3)

      thread.complete(charged)
    })
    expect(PutWfSpecRequest.toJSON(wf.compile())).toMatchSnapshot()
  })
})

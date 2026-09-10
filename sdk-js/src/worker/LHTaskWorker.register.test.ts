import { describe, expect, it } from '@jest/globals'
import { z } from 'zod'
import type { LHConfig } from '../LHConfig'
import { LHTypeAdapterRegistry } from '../common/typeAdapters'
import { VariableType } from '../proto/common_enums'
import { createTaskWorker, TaskSchemaMismatchError } from './LHTaskWorker'

function stubConfig(client: Record<string, unknown>): LHConfig {
  return {
    getTaskWorkerId: () => 'test-worker',
    getTaskWorkerVersion: () => undefined,
    getNumWorkerThreads: () => 8,
    getOauthProvider: () => undefined,
    isOauth: () => false,
    getApiBootstrapHost: () => 'localhost',
    getApiBootstrapPort: () => '2023',
    getTypeAdapterRegistry: () => LHTypeAdapterRegistry.empty(),
    createTransport: () => ({ close: () => undefined }),
    createClientForTransport: () => client,
  } as unknown as LHConfig
}

describe('registerTaskDef', () => {
  it('registers the return type and description alongside the inputs', async () => {
    const puts: unknown[] = []
    const client = {
      putTaskDef: async (request: unknown) => {
        puts.push(request)
        return { id: { name: 'greet' } }
      },
    }
    const worker = createTaskWorker((name: string) => `Hello, ${name}!`, 'greet', stubConfig(client), {
      inputVars: { name: z.string() },
      outputSchema: z.string(),
      description: 'says hello',
    })

    await worker.registerTaskDef()

    const request = puts[0] as {
      name: string
      inputVars: Array<{ name: string }>
      returnType?: { returnType?: { definedType: { oneofKind: string; primitiveType?: VariableType } } }
      description?: string
    }
    expect(request.name).toBe('greet')
    expect(request.inputVars.map(v => v.name)).toEqual(['name'])
    expect(request.description).toBe('says hello')
    expect(request.returnType?.returnType?.definedType).toEqual({
      oneofKind: 'primitiveType',
      primitiveType: VariableType.STR,
    })
  })

  it('omits the return type for a void task', async () => {
    const puts: unknown[] = []
    const client = {
      putTaskDef: async (request: unknown) => {
        puts.push(request)
        return { id: { name: 'fire' } }
      },
    }
    const worker = createTaskWorker(() => undefined, 'fire', stubConfig(client), { inputVars: {} })

    await worker.registerTaskDef()

    expect((puts[0] as { returnType?: unknown }).returnType).toBeUndefined()
  })

  it('surfaces signature drift when the TaskDef already exists', async () => {
    const alreadyExists = Object.assign(new Error('exists'), { code: 'ALREADY_EXISTS' })
    const client = {
      putTaskDef: async () => {
        throw alreadyExists
      },
      getTaskDef: async () => ({
        inputVars: [{ name: 'name' }, { name: 'age' }],
      }),
    }
    const worker = createTaskWorker((name: string) => name, 'greet', stubConfig(client), {
      inputVars: { name: z.string() },
    })

    await expect(worker.registerTaskDef()).rejects.toThrow(TaskSchemaMismatchError)
  })
})

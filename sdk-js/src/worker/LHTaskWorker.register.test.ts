import { afterEach, describe, expect, it, jest } from '@jest/globals'
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

describe('initial worker registration retries', () => {
  afterEach(() => {
    jest.useRealTimers()
    jest.restoreAllMocks()
  })

  it('retries startup failures after one second and uses the normal interval after success', async () => {
    jest.useFakeTimers()
    jest.spyOn(console, 'error').mockImplementation(() => {})
    let attempts = 0
    const registerTaskWorker = jest.fn(async () => {
      attempts++
      if (attempts <= 2 || attempts === 4) throw new Error('unavailable')
      return { yourHosts: [] }
    })
    const worker = createTaskWorker(() => undefined, 'retry-task', stubConfig({ registerTaskWorker }), {
      inputVars: {},
    })
    try {
      await worker.start()
      expect(attempts).toBe(1)
      await jest.advanceTimersByTimeAsync(999)
      expect(attempts).toBe(1)
      await jest.advanceTimersByTimeAsync(1)
      expect(attempts).toBe(2)
      await jest.advanceTimersByTimeAsync(1000)
      expect(attempts).toBe(3)
      await jest.advanceTimersByTimeAsync(14_999)
      expect(attempts).toBe(3)
      await jest.advanceTimersByTimeAsync(1)
      expect(attempts).toBe(4)
      await jest.advanceTimersByTimeAsync(14_999)
      expect(attempts).toBe(4)
      await jest.advanceTimersByTimeAsync(1)
      expect(attempts).toBe(5)
    } finally {
      await worker.close()
    }
    expect(jest.getTimerCount()).toBe(0)
  })

  it('keeps the configured interval when the first registration succeeds', async () => {
    jest.useFakeTimers()
    const registerTaskWorker = jest.fn(async () => ({ yourHosts: [] }))
    const worker = createTaskWorker(() => undefined, 'retry-task', stubConfig({ registerTaskWorker }), {
      inputVars: {},
      heartbeatIntervalMs: 2000,
    })
    try {
      await worker.start()
      await jest.advanceTimersByTimeAsync(1999)
      expect(registerTaskWorker).toHaveBeenCalledTimes(1)
      await jest.advanceTimersByTimeAsync(1)
      expect(registerTaskWorker).toHaveBeenCalledTimes(2)
    } finally {
      await worker.close()
    }
  })

  it('does not overlap a pending registration or schedule retries after closing', async () => {
    jest.useFakeTimers()
    let resolveRegistration!: (response: { yourHosts: [] }) => void
    const registerTaskWorker = jest.fn(
      () =>
        new Promise<{ yourHosts: [] }>(resolve => {
          resolveRegistration = resolve
        })
    )
    const worker = createTaskWorker(() => undefined, 'retry-task', stubConfig({ registerTaskWorker }), {
      inputVars: {},
    })
    const starting = worker.start()
    await jest.advanceTimersByTimeAsync(60_000)
    expect(registerTaskWorker).toHaveBeenCalledTimes(1)
    await worker.close()
    resolveRegistration({ yourHosts: [] })
    await starting
    expect(jest.getTimerCount()).toBe(0)
  })
})

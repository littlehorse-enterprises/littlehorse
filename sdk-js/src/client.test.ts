import { describe, expect, it } from '@jest/globals'
import type { LittleHorseClient } from './proto/service.client'
import { promisifyClient } from './client'

function fakeClient(capture: Array<Record<string, string> | undefined>) {
  return {
    whoami: (_input: unknown, options?: { meta?: Record<string, string> }) => {
      capture.push(options?.meta)
      return { response: Promise.resolve({ id: 'me' }) }
    },
  } as unknown as LittleHorseClient
}

describe('promisifyClient token provider', () => {
  it('mints a fresh token per call', async () => {
    const seen: Array<Record<string, string> | undefined> = []
    let counter = 0
    const client = promisifyClient(fakeClient(seen), {
      defaultOptions: { meta: { tenantId: 'default' } },
      resourceExhaustedRetryEnabled: false,
      tokenProvider: async () => `token-${++counter}`,
    })

    await client.whoami({})
    await client.whoami({})

    expect(seen[0]?.authorization).toBe('Bearer token-1')
    expect(seen[1]?.authorization).toBe('Bearer token-2')
    expect(seen[0]?.tenantId).toBe('default')
  })

  it('lets an explicit per-call authorization win over the provider', async () => {
    const seen: Array<Record<string, string> | undefined> = []
    const client = promisifyClient(fakeClient(seen), {
      defaultOptions: { meta: {} },
      resourceExhaustedRetryEnabled: false,
      tokenProvider: async () => 'minted',
    })

    await client.whoami({}, { meta: { authorization: 'Bearer explicit' } })

    expect(seen[0]?.authorization).toBe('Bearer explicit')
  })

  it('sends anonymously when the provider yields nothing', async () => {
    const seen: Array<Record<string, string> | undefined> = []
    const client = promisifyClient(fakeClient(seen), {
      defaultOptions: { meta: {} },
      resourceExhaustedRetryEnabled: false,
      tokenProvider: async () => undefined,
    })

    await client.whoami({})

    expect(seen[0]?.authorization).toBeUndefined()
  })
})

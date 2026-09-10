import { describe, expect, it } from '@jest/globals'
import { LHConfig } from '../LHConfig'
import { Workflow } from './Workflow'

describe('Workflow registration', () => {
  it('registers through the authenticated client', async () => {
    const calls: string[] = []
    const client = {
      putExternalEventDef: async () => {
        calls.push('putExternalEventDef')
        return {}
      },
      putWorkflowEventDef: async () => {
        calls.push('putWorkflowEventDef')
        return {}
      },
      putWfSpec: async () => {
        calls.push('putWfSpec')
        return {}
      },
    }
    const config = {
      getAuthenticatedClient: async () => {
        calls.push('getAuthenticatedClient')
        return client
      },
      getClient: () => {
        calls.push('getClient')
        return client
      },
    } as unknown as LHConfig

    const wf = Workflow.newWorkflow('auth-pin', thread => {
      thread.declareStr('x')
    })
    await wf.registerWfSpec(config)

    expect(calls).toContain('getAuthenticatedClient')
    expect(calls).not.toContain('getClient')
    expect(calls[calls.length - 1]).toBe('putWfSpec')
  })
})

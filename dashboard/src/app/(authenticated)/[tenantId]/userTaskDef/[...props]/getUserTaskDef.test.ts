import { lhClient } from '@/app/lhClient'
import { getUserTaskDef } from './getUserTaskDef'

jest.mock('@/app/lhClient', () => ({ lhClient: jest.fn() }))

describe('getUserTaskDef', () => {
  const client = { getUserTaskDef: jest.fn(), getLatestUserTaskDef: jest.fn() }

  beforeEach(() => {
    jest.resetAllMocks()
    ;(lhClient as jest.Mock).mockResolvedValue(client)
  })

  it('fetches the requested version', async () => {
    await getUserTaskDef({ name: 'my-form', version: '0', tenantId: 'default' })

    expect(client.getUserTaskDef).toHaveBeenCalledWith({ name: 'my-form', version: 0 })
    expect(client.getLatestUserTaskDef).not.toHaveBeenCalled()
  })

  it.each([undefined, 'latest'])('fetches the latest version when the version is %p', async version => {
    await getUserTaskDef({ name: 'my-form', version, tenantId: 'default' })

    expect(client.getLatestUserTaskDef).toHaveBeenCalledWith({ name: 'my-form' })
    expect(client.getUserTaskDef).not.toHaveBeenCalled()
  })
})

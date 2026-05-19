import { getServerVersion } from './getServerVersion'

jest.mock('@/app/lhClient', () => ({
  lhClient: jest.fn(),
}))

import { lhClient } from '@/app/lhClient'

const mockLhClient = lhClient as jest.MockedFunction<typeof lhClient>

describe('getServerVersion', () => {
  function mockGetServerVersion(version: Record<string, unknown>) {
    mockLhClient.mockResolvedValue({ getServerVersion: jest.fn().mockResolvedValue(version) } as any)
  }

  it('formats full version with patch and pre-release', async () => {
    mockGetServerVersion({ majorVersion: 1, minorVersion: 2, patchVersion: 3, preReleaseIdentifier: 'SNAPSHOT' })
    expect(await getServerVersion('my-tenant')).toBe('1.2.3-SNAPSHOT')
  })

  it('formats version without pre-release identifier', async () => {
    mockGetServerVersion({ majorVersion: 1, minorVersion: 2, patchVersion: 3 })
    expect(await getServerVersion('my-tenant')).toBe('1.2.3')
  })

  it('formats version without patch', async () => {
    mockGetServerVersion({ majorVersion: 1, minorVersion: 2 })
    expect(await getServerVersion('my-tenant')).toBe('1.2')
  })

  it('formats version without patch but with pre-release', async () => {
    mockGetServerVersion({ majorVersion: 1, minorVersion: 2, preReleaseIdentifier: 'SNAPSHOT' })
    expect(await getServerVersion('my-tenant')).toBe('1.2-SNAPSHOT')
  })

  it('formats version with patch 0', async () => {
    mockGetServerVersion({ majorVersion: 1, minorVersion: 0, patchVersion: 0 })
    expect(await getServerVersion('my-tenant')).toBe('1.0.0')
  })
})

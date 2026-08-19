/**
 * @jest-environment node
 */
import { getToken } from 'next-auth/jwt'
import { NextRequest } from 'next/server'
import { GET } from '../route'

jest.mock('next-auth/jwt', () => ({ getToken: jest.fn() }))

const mockedGetToken = jest.mocked(getToken)

const request = () => new NextRequest('http://localhost:3000/api/auth/federated-logout')

const call = async () => (await GET(request())).json()

describe('federated logout', () => {
  const env = process.env

  beforeEach(() => {
    jest.resetAllMocks()
    process.env = {
      ...env,
      KEYCLOAK_ISSUER_URI: 'http://keycloak:8888/realms/lh',
      NEXTAUTH_URL: 'http://localhost:3000',
    }
  })

  afterAll(() => {
    process.env = env
  })

  it('sends the browser to the provider so its SSO cookies are cleared', async () => {
    mockedGetToken.mockResolvedValue({ idToken: 'an-id-token' })

    const { url } = await call()
    const logout = new URL(url)

    expect(logout.origin + logout.pathname).toBe('http://keycloak:8888/realms/lh/protocol/openid-connect/logout')
    expect(logout.searchParams.get('id_token_hint')).toBe('an-id-token')
    expect(logout.searchParams.get('post_logout_redirect_uri')).toBe('http://localhost:3000/api/auth/signin')
  })

  it('does nothing when authentication is disabled', async () => {
    delete process.env.KEYCLOAK_ISSUER_URI
    mockedGetToken.mockResolvedValue({ idToken: 'an-id-token' })

    await expect(call()).resolves.toEqual({ url: null })
  })

  it('does nothing when the session has no id token', async () => {
    mockedGetToken.mockResolvedValue({})

    await expect(call()).resolves.toEqual({ url: null })
  })

  it('does nothing when there is no session at all', async () => {
    mockedGetToken.mockResolvedValue(null)

    await expect(call()).resolves.toEqual({ url: null })
  })
})

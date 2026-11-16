import { afterAll, beforeAll, describe, expect, it } from '@jest/globals'
import { execFileSync } from 'child_process'
import { mkdtempSync, readFileSync, rmSync } from 'fs'
import { createServer, Server } from 'https'
import type { AddressInfo } from 'net'
import { tmpdir } from 'os'
import { join } from 'path'
import { OAuthCredentialsProvider, OAuthError } from './oauth'

interface ReceivedRequest {
  method?: string
  path?: string
  body: string
  authorization?: string
}

/** HTTPS server whose self-signed certificate nothing trusts by default. */
function startServer(
  key: string,
  cert: string,
  route: (path: string, origin: string) => { status: number; location?: string; body?: string }
): Promise<{ server: Server; origin: string; received: ReceivedRequest[] }> {
  const received: ReceivedRequest[] = []
  const server = createServer({ key, cert }, (req, res) => {
    let body = ''
    req.on('data', chunk => (body += chunk))
    req.on('end', () => {
      received.push({ method: req.method, path: req.url, body, authorization: req.headers.authorization })
      const reply = route(req.url ?? '', origin)
      if (reply.location) res.setHeader('Location', reply.location)
      if (reply.body) res.setHeader('Content-Type', 'application/json')
      res.writeHead(reply.status)
      res.end(reply.body)
    })
  })
  let origin = ''
  return new Promise(resolve =>
    server.listen(0, 'localhost', () => {
      origin = `https://localhost:${(server.address() as AddressInfo).port}`
      resolve({ server, origin, received })
    })
  )
}

const TOKEN = JSON.stringify({ access_token: 'ca-token', expires_in: 3600 })

describe('OAuth token requests with LHC_OAUTH_CA_CERT', () => {
  let directory: string
  let caCert: string
  let idp: Awaited<ReturnType<typeof startServer>>
  let otherOrigin: Awaited<ReturnType<typeof startServer>>

  beforeAll(async () => {
    directory = mkdtempSync(join(tmpdir(), 'lh-oauth-ca-'))
    execFileSync(
      'openssl',
      [
        'req',
        '-x509',
        '-newkey',
        'rsa:2048',
        '-nodes',
        '-days',
        '1',
        '-subj',
        '/CN=localhost',
        '-addext',
        'subjectAltName=DNS:localhost',
        '-keyout',
        join(directory, 'key.pem'),
        '-out',
        join(directory, 'cert.pem'),
      ],
      { stdio: 'ignore' }
    )
    const key = readFileSync(join(directory, 'key.pem'), 'utf8')
    caCert = readFileSync(join(directory, 'cert.pem'), 'utf8')

    otherOrigin = await startServer(key, caCert, () => ({ status: 200, body: TOKEN }))
    idp = await startServer(key, caCert, path => {
      switch (path) {
        case '/token':
          return { status: 200, body: TOKEN }
        case '/no-content':
          return { status: 204 }
        case '/temporary':
          return { status: 307, location: '/token' }
        case '/permanent':
          return { status: 308, location: '/token' }
        case '/found':
          return { status: 302, location: '/token' }
        case '/loop':
          return { status: 307, location: '/loop' }
        case '/cross-origin':
          return { status: 307, location: `${otherOrigin.origin}/token` }
        default:
          return { status: 404 }
      }
    })
  })

  afterAll(() => {
    idp?.server.close()
    otherOrigin?.server.close()
    rmSync(directory, { recursive: true, force: true })
  })

  function provider(path: string, withCa = true): OAuthCredentialsProvider {
    idp.received.length = 0
    otherOrigin.received.length = 0
    return new OAuthCredentialsProvider({
      clientId: 'worker',
      clientSecret: 'secret',
      tokenEndpoint: `${idp.origin}${path}`,
      caCert: withCa ? caCert : undefined,
    })
  }

  it('trusts the configured CA', async () => {
    await expect(provider('/token').getToken()).resolves.toBe('ca-token')
  })

  it('rejects the token endpoint without the CA', async () => {
    await expect(provider('/token', false).getToken()).rejects.toThrow(OAuthError)
  })

  it('rejects a 204 response instead of crashing the process', async () => {
    await expect(provider('/no-content').getToken()).rejects.toThrow(/did not contain an access_token/)
  })

  it.each(['/temporary', '/permanent'])('follows a same-origin redirect from %s with the same request', async path => {
    await expect(provider(path).getToken()).resolves.toBe('ca-token')

    expect(idp.received.map(r => r.path)).toEqual([path, '/token'])
    for (const request of idp.received) {
      expect(request.method).toBe('POST')
      expect(request.body).toBe('grant_type=client_credentials')
      expect(request.authorization).toMatch(/^Basic /)
    }
  })

  it('stops a redirect loop', async () => {
    await expect(provider('/loop').getToken()).rejects.toThrow(/redirected more than 5 times/)
    expect(idp.received).toHaveLength(6)
  })

  it('does not send credentials to another origin', async () => {
    await expect(provider('/cross-origin').getToken()).rejects.toThrow(/redirected to another origin/)
    expect(otherOrigin.received).toHaveLength(0)
  })

  it('rejects redirects that would turn the POST into a GET', async () => {
    await expect(provider('/found').getToken()).rejects.toThrow(/only 307 and 308 are followed/)
  })
})

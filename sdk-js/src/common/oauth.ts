import { X509Certificate } from 'crypto'
import type { OutgoingHttpHeaders } from 'http'
import { request } from 'https'
import { rootCertificates } from 'tls'
import { LHError } from './errors'

/**
 * OAuth2 client-credentials support (Java: common/auth).
 *
 * The server accepts a bearer token; this fetches one from the configured
 * token endpoint and keeps it fresh. Tokens are cached and reused until they
 * are close to expiring, because minting one per RPC would add a round trip
 * to every call.
 */

export interface OAuthOptions {
  clientId: string
  clientSecret: string
  /** The OAuth2 token endpoint (`.../oauth2/token`). */
  tokenEndpoint: string
  /**
   * Refresh this many ms before actual expiry, so a token never expires
   * mid-flight on a slow call. Defaults to 30s.
   */
  refreshSkewMs?: number
  /** PEM CA certificates trusted for the token endpoint, besides Node's bundled CAs. */
  caCert?: string
  /** Injectable for tests; defaults to global fetch. */
  fetchImpl?: typeof fetch
}

export interface TokenStatus {
  token: string
  /** Epoch ms at which the token stops being valid. */
  expiresAt: number
  clientId: string
}

export class OAuthError extends LHError {}

const DEFAULT_REFRESH_SKEW_MS = 30_000
const MAX_REDIRECTS = 5
const BODYLESS_STATUSES = new Set([204, 205, 304])
const REDIRECT_STATUSES = new Set([301, 302, 303, 307, 308])

interface RawResponse {
  status: number
  location?: string
  body: Buffer
}

function requestOnce(url: URL, init: RequestInit | undefined, ca: string[]): Promise<RawResponse> {
  return new Promise((resolve, reject) => {
    const req = request(
      url,
      { method: init?.method ?? 'GET', headers: init?.headers as OutgoingHttpHeaders, ca },
      res => {
        const chunks: Buffer[] = []
        res.on('data', (chunk: Buffer) => chunks.push(chunk))
        res.on('end', () =>
          resolve({ status: res.statusCode ?? 0, location: res.headers.location, body: Buffer.concat(chunks) })
        )
        res.on('error', reject)
      }
    )
    req.on('error', reject)
    if (typeof init?.body === 'string') {
      req.write(init.body)
    }
    req.end()
  })
}

/**
 * A fetch that also trusts `caCert`. Global fetch can't take a custom CA
 * without the undici package, so this goes through https.request. It follows
 * only same-origin 307/308 redirects, which keep the method, body and
 * credentials, and rejects any other redirect instead of sending the
 * credentials elsewhere.
 */
export function fetchTrustingCa(caCert: string): typeof fetch {
  try {
    new X509Certificate(caCert)
  } catch (err) {
    throw new OAuthError(`Invalid OAuth CA certificate: ${(err as Error).message}`)
  }
  const ca = [...rootCertificates, caCert]

  return (async (input: string | URL, init?: RequestInit) => {
    let url = new URL(input)
    for (let redirects = 0; ; redirects++) {
      const res = await requestOnce(url, init, ca)
      if (!REDIRECT_STATUSES.has(res.status)) {
        return new Response(BODYLESS_STATUSES.has(res.status) ? null : res.body, { status: res.status })
      }
      if (res.status !== 307 && res.status !== 308) {
        throw new OAuthError(`OAuth token endpoint redirected with HTTP ${res.status}; only 307 and 308 are followed`)
      }
      if (redirects === MAX_REDIRECTS) {
        throw new OAuthError(`OAuth token endpoint redirected more than ${MAX_REDIRECTS} times`)
      }
      const next = new URL(res.location ?? '', url)
      if (next.origin !== url.origin) {
        throw new OAuthError(
          `OAuth token endpoint redirected to another origin (${next.origin}); set LHC_OAUTH_ACCESS_TOKEN_URL to the final URL`
        )
      }
      url = next
    }
  }) as typeof fetch
}

export class OAuthCredentialsProvider {
  private cached?: TokenStatus
  /** In-flight fetch, so concurrent callers share one request. */
  private pending?: Promise<TokenStatus>
  private readonly refreshSkewMs: number
  private readonly fetchImpl: typeof fetch
  /** Number of token endpoint round trips; used by tests. */
  fetchCount = 0

  constructor(private readonly options: OAuthOptions) {
    this.refreshSkewMs = options.refreshSkewMs ?? DEFAULT_REFRESH_SKEW_MS
    this.fetchImpl =
      options.fetchImpl ?? (options.caCert !== undefined ? fetchTrustingCa(options.caCert) : globalThis.fetch)
    if (typeof this.fetchImpl !== 'function') {
      throw new OAuthError('No fetch implementation available for OAuth; pass fetchImpl explicitly')
    }
  }

  /** True when the cached token is missing or close enough to expiry. */
  private needsRefresh(now: number): boolean {
    return this.cached === undefined || this.cached.expiresAt - this.refreshSkewMs <= now
  }

  /** A valid access token, fetching or refreshing as needed. */
  async getToken(now: number = Date.now()): Promise<string> {
    if (!this.needsRefresh(now)) {
      return this.cached!.token
    }
    // Collapse concurrent refreshes into one request.
    this.pending ??= this.fetchToken(now).finally(() => {
      this.pending = undefined
    })
    const status = await this.pending
    return status.token
  }

  /** The cached token's metadata, without triggering a fetch. */
  getCachedToken(): TokenStatus | undefined {
    return this.cached
  }

  private async fetchToken(now: number): Promise<TokenStatus> {
    // Client credentials go in the Authorization header (HTTP Basic), which
    // is what Java's ClientSecretBasic sends.
    const basic = Buffer.from(`${this.options.clientId}:${this.options.clientSecret}`).toString('base64')

    let response: Response
    try {
      response = await this.fetchImpl(this.options.tokenEndpoint, {
        method: 'POST',
        headers: {
          Authorization: `Basic ${basic}`,
          'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: 'grant_type=client_credentials',
      })
    } catch (err) {
      throw new OAuthError(`Could not reach the OAuth token endpoint: ${(err as Error).message}`)
    }

    this.fetchCount++

    if (!response.ok) {
      throw new OAuthError(`OAuth token request failed with HTTP ${response.status}`)
    }

    const body = (await response.json().catch(() => undefined)) as
      | { access_token?: string; expires_in?: number }
      | undefined
    if (!body?.access_token) {
      throw new OAuthError('OAuth token response did not contain an access_token')
    }

    this.cached = {
      token: body.access_token,
      // A response without expires_in is treated as already expiring, so it
      // is re-fetched rather than cached forever.
      expiresAt: now + (body.expires_in ?? 0) * 1000,
      clientId: this.options.clientId,
    }
    return this.cached
  }
}

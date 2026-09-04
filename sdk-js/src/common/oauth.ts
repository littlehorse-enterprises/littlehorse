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
    this.fetchImpl = options.fetchImpl ?? globalThis.fetch
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

    const body = (await response.json()) as { access_token?: string; expires_in?: number }
    if (!body.access_token) {
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

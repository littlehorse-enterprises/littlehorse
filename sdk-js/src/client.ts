import type { RpcMetadata, RpcOptions, UnaryCall } from '@protobuf-ts/runtime-rpc'
import type { PartialMessage } from '@protobuf-ts/runtime'
import type { ILittleHorseClient, LittleHorseClient } from './proto/service.client'
import { LittleHorse } from './proto/service'
import { getRetryDelayMs, isResourceExhausted } from './grpcRetry'

/**
 * Transforms the generated `@protobuf-ts` client interface (whose unary methods
 * return `UnaryCall` objects) into a Promise-based client whose unary methods
 * resolve directly to the response message. Streaming methods (e.g. `pollTask`)
 * are left untouched.
 *
 * Requests are accepted as `PartialMessage`, matching what Java's builders let
 * you leave out. The generated types require every repeated and map field to be
 * present, and omitting one fails deep inside serialization with an opaque
 * "Cannot read properties of undefined (reading 'length')" — so requests are
 * normalized through the message's own `create()` before being sent.
 */
export type LHPublicClient = {
  [K in keyof ILittleHorseClient]: ILittleHorseClient[K] extends (
    input: infer I,
    options?: RpcOptions
  ) => UnaryCall<infer _Req, infer O>
    ? (input: PartialMessage<I & object>, options?: RpcOptions) => Promise<O>
    : ILittleHorseClient[K]
}

/** Request message types by client method name, for defaulting inputs. */
const REQUEST_TYPES = new Map(LittleHorse.methods.map(method => [method.localName, method.I]))

export interface PromisifyClientOptions {
  defaultOptions: RpcOptions
  resourceExhaustedRetryEnabled: boolean
  /**
   * Minted per unary call, so a long-held client never outlives its token
   * (Java refreshes per RPC through CallCredentials). An authorization set in
   * per-call options wins.
   */
  tokenProvider?: () => Promise<string | undefined>
}

function mergeMeta(base?: RpcMetadata, override?: RpcMetadata): RpcMetadata {
  return { ...(base ?? {}), ...(override ?? {}) }
}

function mergeOptions(base: RpcOptions, override?: RpcOptions): RpcOptions {
  return {
    ...base,
    ...(override ?? {}),
    meta: mergeMeta(base.meta, override?.meta),
  }
}

function sleep(ms: number): Promise<void> {
  return new Promise(resolve => setTimeout(resolve, ms))
}

export function promisifyClient(client: LittleHorseClient, options: PromisifyClientOptions): LHPublicClient {
  const { defaultOptions, resourceExhaustedRetryEnabled, tokenProvider } = options

  return new Proxy(client, {
    get(target, prop, receiver) {
      const value = Reflect.get(target, prop, receiver)
      if (typeof value !== 'function') return value

      const methodName = String(prop)

      // pollTask is a duplex streaming RPC; return the underlying call so the
      // caller can drive `requests`/`responses` directly.
      if (methodName === 'pollTask') {
        return (callOptions?: RpcOptions) => value.call(target, mergeOptions(defaultOptions, callOptions))
      }

      return async (input: unknown, callOptions?: RpcOptions) => {
        const merged = mergeOptions(defaultOptions, callOptions)
        if (tokenProvider !== undefined && merged.meta?.['authorization'] === undefined) {
          const token = await tokenProvider()
          if (token !== undefined) {
            merged.meta = { ...merged.meta, authorization: `Bearer ${token}` }
          }
        }
        // Fills in the fields protobuf-ts requires but Java's builders default.
        const requestType = REQUEST_TYPES.get(methodName)
        const request = requestType === undefined ? input : requestType.create(input as never)

        for (;;) {
          const call = value.call(target, request, merged) as UnaryCall
          try {
            return await call.response
          } catch (error) {
            if (resourceExhaustedRetryEnabled && isResourceExhausted(error)) {
              const delayMs = getRetryDelayMs(error)
              if (delayMs !== undefined) {
                await sleep(delayMs)
                continue
              }
            }
            throw error
          }
        }
      }
    },
  }) as unknown as LHPublicClient
}

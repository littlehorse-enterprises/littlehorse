import type { RpcMetadata, RpcOptions, UnaryCall } from '@protobuf-ts/runtime-rpc'
import type { ILittleHorseClient, LittleHorseClient } from './proto/service.client'
import { getRetryDelayMs, isResourceExhausted } from './grpcRetry'

/**
 * Transforms the generated `@protobuf-ts` client interface (whose unary methods
 * return `UnaryCall` objects) into a Promise-based client whose unary methods
 * resolve directly to the response message. Streaming methods (e.g. `pollTask`)
 * are left untouched.
 */
export type LHPublicClient = {
  [K in keyof ILittleHorseClient]: ILittleHorseClient[K] extends (
    input: infer I,
    options?: RpcOptions
  ) => UnaryCall<infer _Req, infer O>
    ? (input: I, options?: RpcOptions) => Promise<O>
    : ILittleHorseClient[K]
}

export interface PromisifyClientOptions {
  defaultOptions: RpcOptions
  resourceExhaustedRetryEnabled: boolean
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
  const { defaultOptions, resourceExhaustedRetryEnabled } = options

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

        for (;;) {
          const call = value.call(target, input, merged) as UnaryCall
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

import { WfSpec } from 'littlehorse-client/proto'

type NodeLike = {
  outgoingEdges?: { sinkNodeName: string }[]
  failureHandlers?: { handlerSpecName?: string; handler_spec_name?: string }[]
  node?: {
    $case: string
    value?: Record<string, unknown>
  }
}

/** DFS from entrypoint; collect thread names. wait_for_threads handlers (on node or in value) go to deferred. */
function definitionOrder(spec: WfSpec): string[] {
  const ep = spec.threadSpecs[spec.entrypointThreadName]
  const nodes = ep?.nodes as Record<string, NodeLike> | undefined
  if (!nodes) return []

  const entryId = Object.entries(nodes).find(([, n]) => n.node?.$case === 'entrypoint')?.[0]
  if (!entryId) return []

  const seen = new Set<string>()
  const order: string[] = []
  const deferred: string[] = []
  const map = nodes

  function add(name: string, toDeferred = false) {
    if (!name || seen.has(name)) return
    seen.add(name)
    if (toDeferred) deferred.push(name)
    else order.push(name)
  }

  const visited = new Set<string>()
  function visit(id: string) {
    if (visited.has(id)) return
    visited.add(id)
    const n = map[id]
    if (!n) return
    const inner = n.node
    const isWait = inner?.$case === 'wait_for_threads' || inner?.$case === 'waitForThreads'

    // Failure handlers: defer if this node is wait_for_threads, else add to order
    for (const h of n.failureHandlers ?? []) {
      const name = h.handlerSpecName ?? h.handler_spec_name
      if (name) add(name, isWait)
    }

    const v = inner?.value as { threadSpecName?: string; thread_spec_name?: string; perThreadFailureHandlers?: { handlerSpecName?: string; handler_spec_name?: string }[]; per_thread_failure_handlers?: { handlerSpecName?: string; handler_spec_name?: string }[] } | undefined
    const threadName = v?.threadSpecName ?? v?.thread_spec_name
    const isStartThread = inner?.$case === 'start_thread' || inner?.$case === 'startThread'
    const isStartMultiple = inner?.$case === 'start_multiple_threads' || inner?.$case === 'startMultipleThreads'
    if (isStartThread && threadName) add(threadName)
    if (isStartMultiple && threadName) add(threadName)

    const perThread = v?.perThreadFailureHandlers ?? v?.per_thread_failure_handlers ?? []
    if (isWait) {
      for (const h of perThread) {
        const name = h?.handlerSpecName ?? h?.handler_spec_name
        if (name) add(name, true)
      }
    }

    for (const e of n.outgoingEdges ?? []) if (map[e.sinkNodeName]) visit(e.sinkNodeName)
  }
  visit(entryId)
  return [...order, ...deferred]
}

/** Thread names in WfSpec definition order: entrypoint first, then as referenced in entrypoint graph, then rest alphabetical. */
export function sortThreadNames(spec: WfSpec): string[] {
  const all = Object.keys(spec.threadSpecs)
  const ep = spec.entrypointThreadName
  const ordered = definitionOrder(spec)
  if (ordered.length === 0) {
    const rest = all.filter(n => n !== ep).sort((a, b) => a.localeCompare(b))
    return [...(all.includes(ep) ? [ep] : []), ...rest]
  }
  const result = [...(all.includes(ep) ? [ep] : []), ...ordered.filter(n => n !== ep)]
  const rest = all.filter(n => !result.includes(n)).sort((a, b) => a.localeCompare(b))
  return [...result, ...rest]
}

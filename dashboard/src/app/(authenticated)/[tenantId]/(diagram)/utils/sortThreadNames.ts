/**
 * Sorts WfSpec thread names for display: entrypoint first, then user-named
 * threads (e.g. fetch-docs-branch, index-branch), then exception handlers
 * (exn-handler-*) alphabetically.
 */
export function sortThreadNames(names: string[], entrypointThreadName: string): string[] {
  const entrypoint = entrypointThreadName
  const rest = names.filter(n => n !== entrypoint)
  const userThreads = rest.filter(n => !n.startsWith('exn-handler-')).sort((a, b) => a.localeCompare(b))
  const exnHandlers = rest.filter(n => n.startsWith('exn-handler-')).sort((a, b) => a.localeCompare(b))
  return [
    ...(names.includes(entrypoint) ? [entrypoint] : []),
    ...userThreads,
    ...exnHandlers,
  ]
}

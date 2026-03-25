/** Returns unique values in first-seen order (Set dedupe, stable order). */
export function uniqueInOrder<T>(items: readonly T[]): T[] {
  const seen = new Set<T>()
  const out: T[] = []
  for (const item of items) {
    if (!seen.has(item)) {
      seen.add(item)
      out.push(item)
    }
  }
  return out
}

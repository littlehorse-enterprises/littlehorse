// Semantic equality for proto-JSON: objects compare by content (key order
// irrelevant), arrays element-wise, scalars strictly. Both the canon and
// every testee emit default values, so deep structural equality IS proto
// message equality for this corpus.
export function semanticDiff(expected, actual, path = '$') {
  if (expected === actual) return []
  const te = typeOf(expected)
  const ta = typeOf(actual)
  if (te !== ta) return [`${path}: expected ${te} ${show(expected)}, got ${ta} ${show(actual)}`]
  if (te === 'array') {
    if (expected.length !== actual.length)
      return [`${path}: expected ${expected.length} elements, got ${actual.length}`]
    return expected.flatMap((e, i) => semanticDiff(e, actual[i], `${path}[${i}]`))
  }
  if (te === 'object') {
    const diffs = []
    for (const key of new Set([...Object.keys(expected), ...Object.keys(actual)])) {
      if (!(key in actual)) diffs.push(`${path}.${key}: missing (expected ${show(expected[key])})`)
      else if (!(key in expected)) diffs.push(`${path}.${key}: unexpected (got ${show(actual[key])})`)
      else diffs.push(...semanticDiff(expected[key], actual[key], `${path}.${key}`))
    }
    return diffs
  }
  return [`${path}: expected ${show(expected)}, got ${show(actual)}`]
}

const typeOf = (v) => (v === null ? 'null' : Array.isArray(v) ? 'array' : typeof v)
const show = (v) => JSON.stringify(v)

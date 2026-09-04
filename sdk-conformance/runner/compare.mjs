// Deep equality of the normalized forms IS proto message equality: canon and
// every testee emit default values, and null normalizes to absent.
export function semanticDiff(expected, actual, path = '$') {
  return diff(stripAbsent(expected), stripAbsent(actual), path)
}

/** proto3 JSON: null means "field absent" (protobuf-ts emits null for unset
 * message fields; protobuf-java omits them). */
export function stripAbsent(value) {
  if (Array.isArray(value)) return value.map(stripAbsent)
  if (value !== null && typeof value === 'object')
    return Object.fromEntries(
      Object.entries(value)
        .filter(([, v]) => v !== null)
        .map(([k, v]) => [k, stripAbsent(v)]),
    )
  return value
}

function diff(expected, actual, path) {
  if (expected === actual) return []
  const te = typeOf(expected)
  const ta = typeOf(actual)
  if (te !== ta) return [`${path}: expected ${te} ${show(expected)}, got ${ta} ${show(actual)}`]
  if (te === 'array') {
    if (expected.length !== actual.length)
      return [`${path}: expected ${expected.length} elements, got ${actual.length}`]
    return expected.flatMap((e, i) => diff(e, actual[i], `${path}[${i}]`))
  }
  if (te === 'object') {
    const diffs = []
    for (const key of new Set([...Object.keys(expected), ...Object.keys(actual)])) {
      if (!(key in actual)) diffs.push(`${path}.${key}: missing (expected ${show(expected[key])})`)
      else if (!(key in expected)) diffs.push(`${path}.${key}: unexpected (got ${show(actual[key])})`)
      else diffs.push(...diff(expected[key], actual[key], `${path}.${key}`))
    }
    return diffs
  }
  return [`${path}: expected ${show(expected)}, got ${show(actual)}`]
}

const typeOf = (v) => (v === null ? 'null' : Array.isArray(v) ? 'array' : typeof v)
const show = (v) => JSON.stringify(v)

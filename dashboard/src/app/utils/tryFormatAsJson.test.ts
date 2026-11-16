import { tryFormatAsJson } from './tryFormatAsJson'

describe('tryFormatAsJson', () => {
  it.each([
    [
      '{"id":1859273648201937921,"decimal":1.234567890123456789,"exponent":1e400,"zero":-0}',
      '{\n    "id": 1859273648201937921,\n    "decimal": 1.234567890123456789,\n    "exponent": 1e400,\n    "zero": -0\n}',
    ],
    [
      ' { "nested" : [ {}, [ ], { "ok" : true, "value": null } ] } ',
      '{\n    "nested": [\n        {},\n        [],\n        {\n            "ok": true,\n            "value": null\n        }\n    ]\n}',
    ],
    [
      String.raw`{"text":"punctuation { [ , : ] }, quote \" and slash \\","escaped":"\u0041\n"}`,
      String.raw`{
    "text": "punctuation { [ , : ] }, quote \" and slash \\",
    "escaped": "\u0041\n"
}`,
    ],
    ['[1,2,3]', '[\n    1,\n    2,\n    3\n]'],
    ['{}', '{}'],
    ['[]', '[]'],
    [' 1859273648201937921 ', '1859273648201937921'],
    [' "hello  world" ', '"hello  world"'],
    [' true ', 'true'],
    [' null ', 'null'],
  ])('formats %s without changing its tokens', (input: string, expected: string) => {
    expect(tryFormatAsJson(input)).toBe(expected)
    expect(tryFormatAsJson(expected)).toBe(expected)
  })

  it.each([
    { name: 'arrays', open: '[', close: ']', key: '' },
    { name: 'objects', open: '{', close: '}', key: '"child": ' },
  ])('indents 1,000 nested $name correctly', ({ open, close, key }) => {
    const depth = 1000
    const value = '1859273648201937921'
    const input = (open + key).repeat(depth) + value + close.repeat(depth)
    const output = tryFormatAsJson(input)
    const lines = output.split('\n')

    expect(lines).toHaveLength(depth * 2 + 1)
    expect(lines[0]).toBe(open)
    for (let level = 1; level < depth; level++) {
      expect(lines[level]).toBe('    '.repeat(level) + key + open)
    }
    expect(lines[depth]).toBe('    '.repeat(depth) + key + value)
    for (let level = 0; level < depth; level++) {
      expect(lines[depth + 1 + level]).toBe('    '.repeat(depth - 1 - level) + close)
    }
    expect(output.replace(/\s/g, '')).toBe(input.replace(/\s/g, ''))
    expect(tryFormatAsJson(output)).toBe(output)
    expect(tryFormatAsJson(input.slice(0, -1))).toBe(input.slice(0, -1))
  })

  it.each(['', 'plain text', '{"missing":}', '[1,]', '{"unclosed":"text}', '  invalid  '])(
    'returns invalid JSON unchanged: %s',
    (input: string) => {
      expect(tryFormatAsJson(input)).toBe(input)
    }
  )
})

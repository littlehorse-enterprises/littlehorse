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

  it.each(['', 'plain text', '{"missing":}', '[1,]', '{"unclosed":"text}', '  invalid  '])(
    'returns invalid JSON unchanged: %s',
    (input: string) => {
      expect(tryFormatAsJson(input)).toBe(input)
    }
  )
})

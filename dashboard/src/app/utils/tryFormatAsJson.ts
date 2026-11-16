export function tryFormatAsJson(text: string): string {
  try {
    // Validate only; reserializing the parsed value would round large numbers.
    JSON.parse(text)
  } catch {
    return text
  }

  const tokens = text.match(/"(?:\\.|[^"\\])*"|\S/g)!
  let depth = 0
  let formatted = ''
  for (const [index, token] of tokens.entries()) {
    if (token === '{' || token === '[') {
      depth++
      formatted += token
      if (tokens[index + 1] !== '}' && tokens[index + 1] !== ']') {
        formatted += '\n' + '    '.repeat(depth)
      }
    } else if (token === '}' || token === ']') {
      depth--
      if (tokens[index - 1] !== '{' && tokens[index - 1] !== '[') {
        formatted += '\n' + '    '.repeat(depth)
      }
      formatted += token
    } else if (token === ',') {
      formatted += ',\n' + '    '.repeat(depth)
    } else if (token === ':') {
      formatted += ': '
    } else {
      formatted += token
    }
  }
  return formatted
}

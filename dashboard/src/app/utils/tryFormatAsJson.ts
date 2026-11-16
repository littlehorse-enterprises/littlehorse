import { parse, stringify } from 'lossless-json'

export function tryFormatAsJson(text: string): string {
  try {
    return stringify(parse(text), null, 4) ?? text
  } catch {
    return text
  }
}

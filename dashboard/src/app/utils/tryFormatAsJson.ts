export function tryFormatAsJson(text: string): string {
  try {
    const parsed = JSON.parse(text)
    return JSON.stringify(parsed, null, 4)
  } catch {
    return text
  }
}

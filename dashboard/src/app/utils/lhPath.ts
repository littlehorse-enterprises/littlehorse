import { LHPath } from 'littlehorse-client/proto'

/**
 * Converts an LHPath to a String in a JSONPath-like format
 * @param lhPath an LHPath object
 * @returns a string in a JSONPath-like format
 */
export const lhPathToString = (lhPath: LHPath): string => {
  return lhPath.path.reduce((outputStr, selector) => {
    switch (selector.selectorType?.oneofKind) {
      case 'index':
        return `${outputStr}[${selector.selectorType.index}]`
      case 'key':
        return `${outputStr}.${selector.selectorType.key}`
      default:
        return outputStr
    }
  }, '$')
}

import { Comparator } from 'littlehorse-client/proto'

export const getComparatorSymbol = (comparator: Comparator) => Conditions[comparator]

/** Short natural-language label for diagram edge conditions. */
export const getComparatorLabel = (comparator: Comparator): string => {
  switch (comparator) {
    case Comparator.EQUALS:
      return 'equals'
    case Comparator.NOT_EQUALS:
      return 'not equal to'
    case Comparator.LESS_THAN:
      return 'less than'
    case Comparator.GREATER_THAN:
      return 'greater than'
    case Comparator.LESS_THAN_EQ:
      return 'at most'
    case Comparator.GREATER_THAN_EQ:
      return 'at least'
    case Comparator.IN:
      return 'in'
    case Comparator.NOT_IN:
      return 'not in'
    default:
      return getComparatorSymbol(comparator)
  }
}

export const Conditions: Record<Comparator, string> = {
  [Comparator.LESS_THAN]: '<',
  [Comparator.GREATER_THAN]: '>',
  [Comparator.LESS_THAN_EQ]: '<=',
  [Comparator.GREATER_THAN_EQ]: '>=',
  [Comparator.EQUALS]: '==',
  [Comparator.NOT_EQUALS]: '!=',
  [Comparator.IN]: 'IN',
  [Comparator.NOT_IN]: 'NOT IN',
  [Comparator.UNRECOGNIZED]: '',
}

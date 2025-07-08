import { Comparator } from 'littlehorse-client/proto'

export const getComparatorSymbol = (comparator: Comparator) => Conditions[comparator]

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

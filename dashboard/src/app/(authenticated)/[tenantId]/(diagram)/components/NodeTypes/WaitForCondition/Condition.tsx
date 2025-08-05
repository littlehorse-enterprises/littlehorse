import { getVariable } from '@/app/utils'
import { getComparatorSymbol } from '@/app/utils/comparatorUtils'
import { EdgeCondition } from 'littlehorse-client/proto'

export const Condition = ({ left, right, comparator }: EdgeCondition) => {
  return (
    <div className="absolute mt-1 flex w-full items-center justify-center gap-1 whitespace-nowrap text-center text-[8px]">
      <div className="rounded bg-gray-200 px-1 font-mono text-fuchsia-500">{left && getVariable(left)}</div>
      <div className="rounded bg-gray-200 px-1 text-[12px] font-light tracking-tighter">
        {getComparatorSymbol(comparator)}
      </div>
      <div className="text-nowrap rounded bg-gray-200 px-1">{right && getVariable(right)}</div>
    </div>
  )
}

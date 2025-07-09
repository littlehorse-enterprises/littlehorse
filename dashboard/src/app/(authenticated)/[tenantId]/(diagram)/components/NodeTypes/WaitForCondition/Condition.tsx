import { cn } from '@/components/utils'
import { Comparator } from 'littlehorse-client/proto'
import { getComparatorSymbol } from '@/app/utils/comparatorUtils'

export const Condition = ({
  variableName,
  comparator,
  rightSide,
  className,
}: {
  variableName: string
  comparator: Comparator
  rightSide: string
  className?: string
}) => {
  return (
    <div className={cn(className, 'mt-1 gap-1 text-[8px]')}>
      <div className="rounded bg-gray-200 px-1 font-mono text-fuchsia-500">{variableName}</div>
      <div className="rounded bg-gray-200 px-1 text-[12px] font-light tracking-tighter">
        {getComparatorSymbol(comparator)}
      </div>
      <div className="text-nowrap rounded bg-gray-200 px-1">{rightSide}</div>
    </div>
  )
}

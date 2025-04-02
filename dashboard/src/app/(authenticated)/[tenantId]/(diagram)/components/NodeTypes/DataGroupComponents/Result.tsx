import { OverflowText } from '@/app/(authenticated)/[tenantId]/components/OverflowText'
import { getVariableValue } from '@/app/utils/variables'
import { cn } from '@/components/utils'
import { LHTaskError, LHTaskException, VariableValue } from 'littlehorse-client/proto'
import { FC } from 'react'

export function Result({
  resultString,
  resultMessage,
  variant,
}: {
  resultString: string
  resultMessage: string
  variant?: 'error'
}) {
  return (
    <div className="flex w-full gap-2 rounded-lg border border-black p-1">
      <div
        className={cn('flex flex-1 items-center justify-center rounded-lg bg-gray-300 py-1 text-center text-xs', {
          'bg-red-300': variant === 'error',
        })}
      >
        {resultString}
      </div>
      <div className={'max-w-32 flex-1 text-nowrap rounded-lg border border-black bg-gray-300 px-1 text-center'}>
        <OverflowText text={resultMessage} className="text-xs" variant={variant} />
      </div>
    </div>
  )
}

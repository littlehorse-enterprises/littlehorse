import { OverflowText } from '@/app/(authenticated)/[tenantId]/components/OverflowText'
import { getVariableValue } from '@/app/utils'

import { cn } from '@/components/utils'
import { TaskAttempt } from 'littlehorse-client/proto'
import { FC } from 'react'
import { Entry } from './Entry'

export const Result: FC<{ result?: TaskAttempt['result'] }> = ({ result }) => {
  if (!result) return null
  const resultMessage = result.$case === 'output' ? getVariableValue(result.value) : result.value.message

  return (
    <Entry label="Result:">
      <div className="flex w-full gap-2 rounded-lg border border-black p-1">
        <div
          className={cn('flex flex-1 items-center justify-center rounded-lg bg-gray-300 py-1 text-center text-xs', {
            'bg-red-300': result.$case === 'error',
          })}
        >
          {result.$case.toUpperCase()}
        </div>
        <div className={'max-w-32 flex-1 text-nowrap rounded-lg border border-black bg-gray-300 px-1 text-center'}>
          <OverflowText
            text={resultMessage}
            className="text-xs"
            variant={result.$case === 'error' ? 'error' : undefined}
          />
        </div>
      </div>
    </Entry>
  )
}

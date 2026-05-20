'use client'

import { cn } from '@/components/utils'
import { AccordionContent, AccordionItem, AccordionTrigger } from '@/components/ui/accordion'
import { ThreadSpec } from 'littlehorse-client/proto'
import { FC, useMemo } from 'react'
import { Mutations } from './Mutations'
import { Variables } from './Variables'

type Props = {
  name: string
  spec: ThreadSpec
}

const countMutations = (spec: ThreadSpec) =>
  Object.values(spec.nodes).reduce((count, node) => {
    for (const edge of node.outgoingEdges) {
      count += edge.variableMutations.length
    }
    return count
  }, 0)

export const Thread: FC<Props> = ({ name, spec }) => {
  const mutationCount = useMemo(() => countMutations(spec), [spec])
  const variableCount = spec.variableDefs.length

  return (
    <AccordionItem value={name} className="border-b border-gray-200 last:border-b-0">
      <AccordionTrigger
        className={cn(
          'px-4 py-3 hover:no-underline',
          'hover:bg-muted/40 [&[data-state=open]]:bg-muted/20'
        )}
      >
        <div className="flex min-w-0 items-center gap-3 text-left">
          <span className="truncate font-medium">{name}</span>
          <span className="shrink-0 text-xs text-muted-foreground">
            {variableCount} {variableCount === 1 ? 'variable' : 'variables'} · {mutationCount}{' '}
            {mutationCount === 1 ? 'mutation' : 'mutations'}
          </span>
        </div>
      </AccordionTrigger>
      <AccordionContent className="px-4 pb-4">
        <div className="grid gap-6 sm:grid-cols-2">
          <Variables variableDefs={spec.variableDefs} />
          <Mutations nodes={spec.nodes} />
        </div>
      </AccordionContent>
    </AccordionItem>
  )
}

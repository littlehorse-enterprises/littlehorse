'use client'

import { Accordion } from '@/components/ui/accordion'
import { Separator } from '@/components/ui/separator'
import { WfSpec } from 'littlehorse-client/proto'
import { FC, useMemo } from 'react'
import { Thread } from './Thread'

type Props = {
  spec: WfSpec
}

export const ThreadsSection: FC<Props> = ({ spec }) => {
  const threadNames = useMemo(
    () =>
      Object.keys(spec.threadSpecs).sort((a, b) => {
        if (a === spec.entrypointThreadName) return -1
        if (b === spec.entrypointThreadName) return 1
        return a.localeCompare(b)
      }),
    [spec.threadSpecs, spec.entrypointThreadName]
  )

  return (
    <section className="mb-8">
      <h2 className="text-sm font-medium text-muted-foreground">Thread details</h2>
      <Separator className="mt-2" />
      <div className="mt-4 overflow-hidden rounded-lg border border-gray-200 bg-white">
        <Accordion type="multiple" defaultValue={[spec.entrypointThreadName]}>
          {threadNames.map(name => (
            <Thread key={name} name={name} spec={spec.threadSpecs[name]} />
          ))}
        </Accordion>
      </div>
    </section>
  )
}

'use client'
import { Accordion, AccordionContent, AccordionItem, AccordionTrigger } from '@/components/ui/accordion'
import { ThreadSpec } from 'littlehorse-client/proto'
import { FC } from 'react'
import { Mutations } from './Mutations'
import { Variables } from './Variables'

type Props = {
  name: string
  spec: ThreadSpec
}
export const Thread: FC<Props> = ({ name, spec }) => {
  return (
    <div className="mb-4 rounded border-2 border-slate-100 p-2">
      <Accordion type="single" collapsible>
        <AccordionItem value="thread">
          <AccordionTrigger>
            <h2 className="text-xl">Thread: {name}</h2>
          </AccordionTrigger>
          <AccordionContent className="flex gap-4">
            <Variables variableDefs={spec.variableDefs} />
            <Mutations nodes={spec.nodes} />
          </AccordionContent>
        </AccordionItem>
      </Accordion>
    </div>
  )
}

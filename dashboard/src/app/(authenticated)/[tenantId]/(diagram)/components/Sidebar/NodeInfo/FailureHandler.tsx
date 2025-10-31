import { FailureHandlerDef } from 'littlehorse-client/proto'
import { FC } from 'react'
import { Accordion, AccordionContent, AccordionItem, AccordionTrigger } from '@/components/ui/accordion'
import { useDiagram } from '../../../hooks/useDiagram'
import { ChevronRight, ScanIcon } from 'lucide-react'

export const FailureHandler: FC<{ failureHandlers: FailureHandlerDef[] }> = ({ failureHandlers }) => {
  const { setThread } = useDiagram()

  const sendToFailureHandler = (thread: FailureHandlerDef) => {
    setThread(prev => {
      const current = { name: thread.handlerSpecName }
      return {
        ...prev,
        ...current,
      }
    })
  }
  return (
    <div className="mt-2">
      <Accordion type="single" collapsible>
        <AccordionItem value={'failure-handlers'}>
          <AccordionTrigger>
            <p className="text-[0.75em] text-slate-400">Failure Handlers</p>
          </AccordionTrigger>
          <AccordionContent className="pb-0">
            {failureHandlers.map((handler, index) => (
              <div
                className="flex  cursor-pointer gap-2 text-base font-medium "
                key={index}
                onClick={() => sendToFailureHandler(handler)}
              >
                <div className="mt-1 grow truncate">{handler.handlerSpecName}</div>
                <div className="mt-1 grow pt-1">
                  <ScanIcon size={16} />
                </div>
              </div>
            ))}
          </AccordionContent>
        </AccordionItem>
      </Accordion>
    </div>
  )
}

import { getVariableValue } from '@/app/utils'
import { Accordion, AccordionContent, AccordionItem, AccordionTrigger } from '@/components/ui/accordion'
import { Checkpoint } from 'littlehorse-client/proto'
import { NodeVariable } from './NodeVariable'

export const Checkpoints = ({ checkpoints }: { checkpoints: Checkpoint[] }) => {
  if (!checkpoints.length) return null

  return (
    <Accordion type="single" collapsible>
      <AccordionItem value="checkpoints">
        <AccordionTrigger className="ml-1 pt-1 text-sm font-bold">Checkpoints ({checkpoints.length})</AccordionTrigger>
        <AccordionContent className="mt-2 pb-0">
          {checkpoints.map((checkpoint, index) => (
            <div key={`checkpoint-${index}`} className="mb-2 rounded border border-gray-200 p-1">
              <div className="ml-1 text-sm font-bold ">#{checkpoint.id?.checkpointNumber}</div>
              <NodeVariable label="Created At:" text={checkpoint.createdAt} type="date" />
              {checkpoint.value && <NodeVariable label="value:" text={getVariableValue(checkpoint.value)} />}
              {checkpoint.logs && <NodeVariable label="logs:" text={checkpoint.logs} />}
            </div>
          ))}
        </AccordionContent>
      </AccordionItem>
    </Accordion>
  )
}

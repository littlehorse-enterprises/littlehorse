import { VarNameAndVal } from 'littlehorse-client/proto'
import { Accordion, AccordionContent, AccordionItem, AccordionTrigger } from '@/components/ui/accordion'
import { getVariableValue } from '@/app/utils'
import { OutputModal } from './OutputModal'

export const InputVariables = ({ variables }: { variables: VarNameAndVal[] }) => {
  return (
    <Accordion type="single" collapsible>
      <AccordionItem value="input-variables">
        <AccordionTrigger className=" ml-1 pt-1 text-sm font-bold">Input Variables </AccordionTrigger>
        <AccordionContent className=" mt-2 pb-0 ">
          {variables.map(variable => (
            <div className="ml-1 mt-1 grid grid-cols-2">
              <div key={`variable-${variable.varName}`} className="flex gap-1">
                <p className="rounded bg-gray-100 px-2 py-1 font-mono text-fuchsia-500">{variable.varName}</p>
                {variable.masked && <p className="rounded bg-blue-300 p-1 text-xs ">Masked</p>}
                <p className="flex h-full max-w-96 items-center justify-center font-code">= </p>
                <p className="flex h-full max-w-96 items-center justify-center font-code">
                  {variable.value && getVariableValue(variable.value)}
                </p>
              </div>
                {variable.value && <OutputModal label="" message={JSON.stringify(variable.value)} buttonText="JSON " />}
            </div>
          ))}
        </AccordionContent>
      </AccordionItem>
    </Accordion>
  )
}

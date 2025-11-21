import { VARIABLE_TYPES } from '@/app/constants'
import { getVariableValue } from '@/app/utils'
import { VariableValue } from 'littlehorse-client/proto'
import { Accordion, AccordionContent, AccordionItem, AccordionTrigger } from '@/components/ui/accordion'

export const Results = ({ variables, classTitle }: { variables: [string, VariableValue][]; classTitle: string }) => {
  return (
    <Accordion type="single" collapsible>
      <AccordionItem value="input-variables">
        <AccordionTrigger className={`ml-1 pt-1 text-sm ${classTitle}`}>Results</AccordionTrigger>
        <AccordionContent className=" mt-2 pb-0 ">
          <div className="ml-1 flex  w-fit flex-col gap-1 ">
            {variables.map(([key, variable], index) => {
              if (!variable) return null
              const variableType = variable.value?.$case
              return (
                <div key={`result-${index}-${key}`} className="flex w-full items-center gap-1">
                  <p className="rounded bg-gray-100 px-1 py-1 font-mono text-xs text-fuchsia-500">{key}</p>

                  {variableType && (
                    <span className="rounded bg-yellow-100 p-1 text-xs">{VARIABLE_TYPES[variableType]}</span>
                  )}
                  <p> = </p>
                  <div className={'px-2  text-center text-xs'}>
                    <p className="text-sx">{getVariableValue(variable)}</p>
                  </div>
                </div>
              )
            })}
          </div>
        </AccordionContent>
      </AccordionItem>
    </Accordion>
  )
}

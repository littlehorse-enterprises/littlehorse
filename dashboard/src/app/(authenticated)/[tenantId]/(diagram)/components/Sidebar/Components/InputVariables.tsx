import { VarNameAndVal } from 'littlehorse-client/proto'
import { Accordion, AccordionContent, AccordionItem, AccordionTrigger } from '@/components/ui/accordion'
import { getVariableValue } from '@/app/utils'

export const InputVariables = ({ variables }: { variables: VarNameAndVal[] }) => {
  return (
    <div className="mb-4 mt-4 rounded-lg border border-gray-200 bg-gray-50">
      <Accordion type="single" collapsible defaultValue="input-variables">
        <AccordionItem value="input-variables" className="border-0">
          <AccordionTrigger className="px-4 py-3 text-sm font-semibold text-gray-700 hover:no-underline">
            Input Variables
          </AccordionTrigger>
          <AccordionContent className="px-4 pb-4 pt-0">
            <div className="space-y-2">
              {variables.map(variable => (
                <div
                  key={`variable-${variable.varName}`}
                  className="flex flex-wrap items-center gap-2 rounded-md border border-gray-200 bg-white p-2.5"
                >
                  <span className="rounded bg-gray-100 px-2 py-1 font-mono text-sm font-medium text-purple-600">
                    {variable.varName}
                  </span>
                  <span
                    className={`rounded px-2 py-0.5 text-xs font-medium ${
                      variable.masked ? 'bg-yellow-100 text-yellow-700' : 'bg-green-100 text-green-700'
                    }`}
                  >
                    {variable.masked ? 'Masked' : 'Unmasked'}
                  </span>
                  <span className="flex-1 text-sm text-gray-600">
                    <span className="font-medium text-gray-400">=</span>{' '}
                    {variable.value && getVariableValue(variable.value)}
                  </span>
                </div>
              ))}
            </div>
          </AccordionContent>
        </AccordionItem>
      </Accordion>
    </div>
  )
}

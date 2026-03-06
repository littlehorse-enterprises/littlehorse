import { VarNameAndVal } from 'littlehorse-client/proto'
import { Accordion, AccordionContent, AccordionItem, AccordionTrigger } from '@/components/ui/accordion'
import { IdentifierBadge, MaskedBadge } from '@/components/ui/badge'
import { getVariableValue } from '@/app/utils'
import { tryFormatAsJson } from '@/app/utils/tryFormatAsJson'
import { useModal } from '../../../hooks/useModal'
import { Expand } from 'lucide-react'
import { useCallback, useState } from 'react'

const InputVariableRow = ({ variable }: { variable: VarNameAndVal }) => {
  const { setModal, setShowModal } = useModal()
  const [isHovered, setIsHovered] = useState(false)

  const rawValue = variable.value ? getVariableValue(variable.value) : ''

  const onExpand = useCallback(() => {
    if (!variable.value) return
    setModal({ type: 'output', data: { message: tryFormatAsJson(rawValue), label: variable.varName ?? 'Input' } })
    setShowModal(true)
  }, [variable, setModal, setShowModal])

  return (
    <div
      className="flex flex-wrap items-center gap-2 rounded-md border border-gray-200 bg-white p-2.5"
      onMouseEnter={() => setIsHovered(true)}
      onMouseLeave={() => setIsHovered(false)}
    >
      {variable.varName && <IdentifierBadge name={variable.varName} />}
      {variable.masked && <MaskedBadge />}
      <span className="flex min-w-0 flex-1 items-center gap-1 text-sm text-gray-600">
        <span className="font-medium text-gray-400">=</span>
        <span className="truncate">{rawValue}</span>
        {isHovered && rawValue && (
          <Expand
            className="shrink-0 cursor-pointer text-gray-500 hover:text-gray-700"
            size={14}
            onClick={onExpand}
          />
        )}
      </span>
    </div>
  )
}

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
                <InputVariableRow key={`variable-${variable.varName}`} variable={variable} />
              ))}
            </div>
          </AccordionContent>
        </AccordionItem>
      </Accordion>
    </div>
  )
}

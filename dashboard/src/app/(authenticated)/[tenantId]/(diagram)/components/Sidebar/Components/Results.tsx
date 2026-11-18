import { VARIABLE_TYPES } from '@/app/constants'
import { getVariableValue } from '@/app/utils'
import { tryFormatAsJson } from '@/app/utils/tryFormatAsJson'
import { IdentifierBadge, TypeBadge } from '@/components/ui/badge'
import { VariableValue } from 'littlehorse-client/proto'
import { Accordion, AccordionContent, AccordionItem, AccordionTrigger } from '@/components/ui/accordion'
import { Expand } from 'lucide-react'
import { useCallback, useState } from 'react'
import { useModal } from '../../../hooks/useModal'

const ResultRow = ({ name, variable }: { name: string; variable: VariableValue }) => {
  const { setModal, setShowModal } = useModal()
  const [isHovered, setIsHovered] = useState(false)
  const variableType = variable.value?.oneofKind
  const value = `${getVariableValue(variable) ?? ''}`

  const onExpand = useCallback(() => {
    setModal({ type: 'output', data: { message: tryFormatAsJson(value), label: name } })
    setShowModal(true)
  }, [name, value, setModal, setShowModal])

  return (
    <div
      className="flex w-full min-w-0 items-center gap-1"
      onMouseEnter={() => setIsHovered(true)}
      onMouseLeave={() => setIsHovered(false)}
    >
      <IdentifierBadge name={name} />
      {variableType && <TypeBadge>{VARIABLE_TYPES[variableType]}</TypeBadge>}
      <span className="shrink-0">=</span>
      <span className="min-w-0 flex-1 truncate px-1 text-xs" title={value}>
        {value}
      </span>
      {isHovered && value && (
        <Expand
          aria-label={`Expand ${name}`}
          className="shrink-0 cursor-pointer text-gray-500 hover:text-gray-700"
          size={14}
          onClick={onExpand}
        />
      )}
    </div>
  )
}

export const Results = ({ variables, classTitle }: { variables: [string, VariableValue][]; classTitle: string }) => {
  return (
    <Accordion type="single" collapsible>
      <AccordionItem value="input-variables">
        <AccordionTrigger className={`ml-1 pt-1 text-sm ${classTitle}`}>Results</AccordionTrigger>
        <AccordionContent className=" mt-2 pb-0 ">
          <div className="ml-1 flex w-full min-w-0 flex-col gap-1">
            {variables.map(([key, variable], index) =>
              variable ? <ResultRow key={`result-${index}-${key}`} name={key} variable={variable} /> : null
            )}
          </div>
        </AccordionContent>
      </AccordionItem>
    </Accordion>
  )
}

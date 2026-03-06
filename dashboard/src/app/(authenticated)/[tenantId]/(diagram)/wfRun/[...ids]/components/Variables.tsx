import { ThreadType } from '@/app/(authenticated)/[tenantId]/(diagram)/context'
import { getVariableValue, wfRunIdToPath } from '@/app/utils'
import { AccessLevelBadge, IdentifierBadge, MaskedBadge, RequiredBadge, SearchableBadge } from '@/components/ui/badge'
import { ThreadVarDef, Variable, WfRunId } from 'littlehorse-client/proto'
import { FC, useMemo } from 'react'
import { OverflowText } from '../../../../components/OverflowText'
import { TypeDisplay } from '../../../../components/TypeDisplay'

type VariablesProps = {
  variableDefs: ThreadVarDef[]
  variables: Variable[]
  thread: ThreadType
  wfRunId?: WfRunId
}

export const Variables: FC<VariablesProps> = ({ variableDefs, variables, thread, wfRunId }) => {
  const currentWfRunPath = wfRunId ? wfRunIdToPath(wfRunId) : ''
  const threadVariables = useMemo(() => {
    return variables.filter(v => {
      const sameThread = v?.id?.threadRunNumber === thread.number
      const inherited = wfRunId && v?.id?.wfRunId && wfRunIdToPath(v.id.wfRunId) !== currentWfRunPath
      return sameThread || inherited
    })
  }, [variables, thread.number, wfRunId, currentWfRunPath])

  if (variableDefs.length === 0) return <></>

  const threadLabel = `${thread.name}${thread.number !== 0 ? `-${thread.number}` : ''}`

  return (
    <div>
      <h2 className="text-md mb-2 font-bold">Variables ({threadLabel})</h2>
      {variableDefs.map(variable => (
        <div key={variable.varDef?.name} className="mb-1 flex items-center gap-1">
          {variable.varDef?.name && <IdentifierBadge name={variable.varDef.name} />}
          <TypeDisplay definedType={variable.varDef?.typeDef?.definedType} />
          {variable.required && <RequiredBadge />}
          {variable.searchable && <SearchableBadge />}
          {variable.varDef?.typeDef?.masked && <MaskedBadge />}
          <AccessLevelBadge accessLevel={variable.accessLevel} />
          <span>=</span>
          <span className="truncate">
            <OverflowText className="max-w-96" text={getVariableValueForVariableDef(variable, threadVariables)} />
          </span>
        </div>
      ))}
    </div>
  )
}

const getVariableValueForVariableDef = (variableDef: ThreadVarDef, variables: Variable[]): string => {
  const variable = variables.find(v => v?.id?.name === variableDef.varDef?.name)
  if (!variable || !variable.value) return ''
  return getVariableValue(variable.value)
}

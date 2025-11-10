import LinkWithTenant from '@/app/(authenticated)/[tenantId]/components/LinkWithTenant'
import { getVariableCaseFromType, VARIABLE_CASE_LABELS } from '@/app/utils'
import { FieldLabel } from '@/components/ui/field'
import { cn } from '@/components/utils'
import { StructDefId, VariableType, WfRunVariableAccessLevel } from 'littlehorse-client/proto'
import { FC } from 'react'
import { accessLevels } from '../../../wfSpec/[...props]/components/Variables'

interface FormLabelProps {
  label: string
  variableType?: VariableType
  structDefId?: StructDefId
  accessLevel?: WfRunVariableAccessLevel
  required?: boolean
}
const FormLabel: FC<FormLabelProps> = ({ label, variableType, structDefId, accessLevel, required }) => {
  return (
    <FieldLabel className="flex gap-2">
      <p className="font-semibold">{label}</p>
      <div className="space-x-2">
        {variableType && (
          <span className={cn('rounded bg-blue-300 p-1 text-xs')}>
            {VARIABLE_CASE_LABELS[getVariableCaseFromType(variableType)]}
          </span>
        )}
        {structDefId && (
          <LinkWithTenant
            className={cn('rounded bg-blue-300 p-1 text-xs underline')}
            href={`/structDef/${structDefId.name}/${structDefId.version}`}
          >{`Struct<${structDefId.name},${structDefId.version}>`}</LinkWithTenant>
        )}
        {accessLevel && <span className={cn('rounded bg-green-300 p-1 text-xs')}>{accessLevels[accessLevel]}</span>}
        <span className={cn('rounded p-1 text-xs', { 'bg-red-300': required, 'bg-gray-300': !required })}>
          {required ? 'Required' : 'Optional'}
        </span>
      </div>
    </FieldLabel>
  )
}

export default FormLabel

import LinkWithTenant from '@/app/(authenticated)/[tenantId]/components/LinkWithTenant'
import { getVariableCaseFromType, VARIABLE_CASE_LABELS } from '@/app/utils'
import { AccessLevelBadge, MaskedBadge, OptionalBadge, RequiredBadge, TypeBadge } from '@/components/ui/badge'
import { FieldLabel } from '@/components/ui/field'
import { StructDefId, VariableType, WfRunVariableAccessLevel } from 'littlehorse-client/proto'
import { FC } from 'react'

interface FormLabelProps {
  label: string
  variableType?: VariableType
  structDefId?: StructDefId
  accessLevel?: WfRunVariableAccessLevel
  required?: boolean
  masked?: boolean
}
const FormLabel: FC<FormLabelProps> = ({ label, variableType, structDefId, accessLevel, required, masked }) => {
  return (
    <FieldLabel className="flex gap-2">
      <p className="font-semibold">{label}</p>
      <div className="space-x-2">
        {variableType && <TypeBadge>{VARIABLE_CASE_LABELS[getVariableCaseFromType(variableType)]}</TypeBadge>}
        {structDefId && (
          <TypeBadge>
            <LinkWithTenant
              className="underline"
              href={`/structDef/${structDefId.name}/${structDefId.version}`}
            >{`Struct<${structDefId.name},${structDefId.version}>`}</LinkWithTenant>
          </TypeBadge>
        )}
        {accessLevel && <AccessLevelBadge accessLevel={accessLevel} />}
        {masked && <MaskedBadge />}
        {required ? <RequiredBadge /> : <OptionalBadge />}
      </div>
    </FieldLabel>
  )
}

export default FormLabel

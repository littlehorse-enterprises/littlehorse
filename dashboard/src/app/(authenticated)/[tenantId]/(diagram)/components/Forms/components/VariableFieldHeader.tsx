import { AccessLevelBadge, MaskedBadge, OptionalBadge, RequiredBadge } from '@/components/ui/badge'
import { FieldLabel } from '@/components/ui/field'
import { WfRunVariableAccessLevel } from 'littlehorse-client/proto'
import { FC, ReactNode } from 'react'
import { OverflowText } from '@/app/(authenticated)/[tenantId]/components/OverflowText'

interface VariableFieldHeaderProps {
  name: string
  description?: string
  accessLevel?: WfRunVariableAccessLevel
  required?: boolean
  masked?: boolean
  /** Type-specific badge (primitive, container, or StructDef reference). */
  typeBadge?: ReactNode
  /** Right-aligned control such as the Set Null / Enter Value toggle. */
  action?: ReactNode
}

/** Shared line-1 header for a variable/field: name + variable metadata badges, optional type badge and action. */
const VariableFieldHeader: FC<VariableFieldHeaderProps> = ({
  name,
  description,
  accessLevel,
  required,
  masked,
  typeBadge,
  action,
}) => {
  return (
    <FieldLabel className="flex w-full flex-col gap-2">
      <div className="flex items-center justify-between gap-2">
        <div className="flex items-center gap-2">
          <p className="font-semibold">{name}</p>
          <div className="space-x-2">
            {typeBadge}
            {accessLevel && <AccessLevelBadge accessLevel={accessLevel} />}
            {masked && <MaskedBadge />}
            {required ? <RequiredBadge /> : <OptionalBadge />}
          </div>
        </div>
        {action}
      </div>
      {description && <OverflowText prose className="text-xs font-normal text-muted-foreground" text={description} />}
    </FieldLabel>
  )
}

export default VariableFieldHeader

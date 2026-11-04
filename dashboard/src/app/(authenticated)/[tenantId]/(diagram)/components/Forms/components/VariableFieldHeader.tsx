import { AccessLevelBadge, MaskedBadge, OptionalBadge, RequiredBadge } from '@/components/ui/badge'
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
    <div className="flex w-full flex-col gap-2 text-sm font-medium leading-snug">
      <div className="flex flex-wrap items-center justify-between gap-2">
        <div className="flex min-w-0 flex-1 flex-wrap items-center gap-2">
          <p className="break-words font-semibold">{name}</p>
          <div className="flex flex-wrap gap-2">
            {typeBadge}
            {accessLevel && <AccessLevelBadge accessLevel={accessLevel} />}
            {masked && <MaskedBadge />}
            {required ? <RequiredBadge /> : <OptionalBadge />}
          </div>
        </div>
        {action && <div className="shrink-0">{action}</div>}
      </div>
      {description && <OverflowText prose className="text-xs font-normal text-muted-foreground" text={description} />}
    </div>
  )
}

export default VariableFieldHeader

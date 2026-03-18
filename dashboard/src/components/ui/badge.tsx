import { cn } from '@/components/utils'
import { WfRunVariableAccessLevel } from 'littlehorse-client/proto'
import { FC, ReactNode } from 'react'

export const Badge: FC<{ className?: string; children: ReactNode }> = ({ className, children }) => (
  <span className={cn('rounded p-1 text-xs', className)}>{children}</span>
)

export const TypeBadge: FC<{ children: ReactNode }> = ({ children }) => (
  <Badge className="bg-yellow-100">{children}</Badge>
)

export const RequiredBadge = () => <Badge className="bg-orange-300">Required</Badge>
export const OptionalBadge = () => <Badge className="bg-gray-300">Optional</Badge>
export const SearchableBadge = () => <Badge className="bg-blue-300">Searchable</Badge>
export const MaskedBadge = () => <Badge className="bg-violet-300">Masked</Badge>

const accessLevelLabels: Record<WfRunVariableAccessLevel, string> = {
  PUBLIC_VAR: 'Public',
  INHERITED_VAR: 'Inherited',
  PRIVATE_VAR: 'Private',
  UNRECOGNIZED: '',
}

export const AccessLevelBadge: FC<{ accessLevel: WfRunVariableAccessLevel }> = ({ accessLevel }) => (
  <Badge className="bg-green-300">{accessLevelLabels[accessLevel]}</Badge>
)

export const IdentifierBadge: FC<{ name: string }> = ({ name }) => (
  <Badge className="bg-gray-100 px-2 py-1 font-mono text-fuchsia-500">{name}</Badge>
)

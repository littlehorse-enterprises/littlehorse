import { formatTypeDefinition } from '@/app/utils'
import { routes } from '@/app/routes'
import { TypeBadge } from '@/components/ui/badge'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog'
import { Maximize2 } from 'lucide-react'
import { TypeDefinition } from 'littlehorse-client/proto'
import { FC } from 'react'
import { InlineStructDefDisplay } from './InlineStructDefDisplay'
import LinkWithTenant from './LinkWithTenant'

type Props = {
  definedType?: TypeDefinition['definedType']
}

export const TypeDisplay: FC<Props> = ({ definedType }) => {
  if (!definedType) {
    return <TypeBadge>void</TypeBadge>
  }

  switch (definedType.oneofKind) {
    case 'inlineArrayDef':
      return <TypeBadge>{formatTypeDefinition(definedType)}</TypeBadge>
    case 'inlineMapDef':
      return <TypeBadge>{formatTypeDefinition(definedType)}</TypeBadge>
    case 'inlineStructDef':
      return (
        <Dialog>
          <DialogTrigger asChild>
            <button
              type="button"
              aria-label="Inspect InlineStruct schema"
              className="inline-flex cursor-pointer items-center justify-center rounded align-middle leading-none transition-colors hover:bg-yellow-200 focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2"
            >
              <TypeBadge>
                <span className="inline-flex items-center gap-1">
                  {formatTypeDefinition(definedType)}
                  <Maximize2 aria-hidden="true" className="h-3 w-3" />
                </span>
              </TypeBadge>
            </button>
          </DialogTrigger>
          <DialogContent className="grid max-h-[85vh] max-w-3xl grid-rows-[auto_minmax(0,1fr)] overflow-hidden">
            <DialogHeader>
              <DialogTitle>InlineStruct schema</DialogTitle>
              <DialogDescription>Fields defined directly within this type.</DialogDescription>
            </DialogHeader>
            <div className="overflow-y-auto pr-2">
              <InlineStructDefDisplay inlineStructDef={definedType.inlineStructDef} />
            </div>
          </DialogContent>
        </Dialog>
      )
    case 'primitiveType':
      return <TypeBadge>{formatTypeDefinition(definedType)}</TypeBadge>
    case 'structDefId':
      return (
        <TypeBadge>
          <LinkWithTenant
            className="flex underline"
            href={routes.structDef.detail(definedType.structDefId.name, definedType.structDefId.version)}
          >
            {`Struct<${definedType.structDefId.name},${definedType.structDefId.version}>`}
          </LinkWithTenant>
        </TypeBadge>
      )
    default:
      throw new Error(`Unimplemented type case`)
  }
}

import { TypeDisplay } from '@/app/(authenticated)/[tenantId]/components/TypeDisplay'
import { Fields as StructDefFields } from '@/app/(authenticated)/[tenantId]/structDef/[name]/components/Fields'
import { StructDef, StructDefId } from 'littlehorse-client/proto'
import { FC } from 'react'

type Props = {
  structDefId: StructDefId
  structDef?: StructDef
}

/** The result contract of a Struct-backed UserTaskDef, which has no legacy `fields`. */
export const ResultStructDef: FC<Props> = ({ structDefId, structDef }) => {
  return (
    <div>
      <div className="mb-2 flex items-center gap-2">
        <h2 className="text-md font-bold">Result</h2>
        <TypeDisplay definedType={{ oneofKind: 'structDefId', structDefId }} />
      </div>
      {structDef?.structDef ? (
        <StructDefFields fields={structDef.structDef.fields} />
      ) : (
        <p className="text-sm text-muted-foreground">The fields of this StructDef could not be loaded.</p>
      )}
    </div>
  )
}

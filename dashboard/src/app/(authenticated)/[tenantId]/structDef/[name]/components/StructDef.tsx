'use client'
import { Navigation } from '@/app/(authenticated)/[tenantId]/components/Navigation'
import { Separator } from '@/components/ui/separator'
import { FC } from 'react'
import { StructDef as StructDefProto } from '../../../../../../../../sdk-js/dist/proto/struct_def'
import { Details } from './Details'
import { Fields } from './Fields'

type Props = {
  structDef: StructDefProto
}

export const StructDef: FC<Props> = ({ structDef }) => {
  if (!structDef.structDef?.fields) return

  return (
    <>
      <Navigation href="/?type=StructDef" title="Go back to StructDefs" />
      <Details id={structDef.id} description={structDef.description} />
      <Fields fields={structDef.structDef.fields} />

      <Separator className="my-4" />
    </>
  )
}

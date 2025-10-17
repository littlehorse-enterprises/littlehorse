'use client'
import { Navigation } from '@/app/(authenticated)/[tenantId]/components/Navigation'
import { Separator } from '@/components/ui/separator'
import { StructDef } from 'littlehorse-client/proto'
import { FC } from 'react'
import { Details } from './Details'
import { Fields } from './Fields'

type Props = {
  structDef: StructDef
}

export const StructDefClient: FC<Props> = ({ structDef }) => {
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

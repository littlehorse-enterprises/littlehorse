import { Navigation } from '@/app/(authenticated)/components/Navigation'
import { ExternalEventDef as ExternalEventDefProto } from 'littlehorse-client'
import { FC } from 'react'
import { Details } from './Details'

type Props = {
  spec: ExternalEventDefProto
}
export const ExternalEventDef: FC<Props> = ({ spec }) => {
  return (
    <>
      <Navigation href="/?type=ExternalEventDef" title="Go back to ExternalEventDef" />
      <Details spec={spec} />
    </>
  )
}

import { Navigation } from '@/app/(authenticated)/components/Navigation'
import { ExternalEventDef as ExternalEventDefProto } from 'littlehorse-client/dist/proto/external_event'
import { FC } from 'react'
import { Details } from './Details'
import { InputVars } from './InputVars'

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

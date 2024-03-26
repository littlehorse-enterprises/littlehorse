import { Navigation } from '@/app/(authenticated)/components/Navigation'
import { WfSpec as Spec } from 'littlehorse-client/dist/proto/wf_spec'
import { FC } from 'react'
import { Details } from './Details'
import { Diagram } from './Diagram'
import { Thread } from './Thread'
import { WfRuns } from './WfRuns'

type WfSpecProps = {
  spec: Spec
}
export const WfSpec: FC<WfSpecProps> = ({ spec }) => {
  return (
    <>
      <Navigation href="/" title="Go back to WfSpecs" />
      <Details status={spec.status} id={spec.id} />
      {Object.keys(spec.threadSpecs)
        .reverse()
        .map(name => (
          <Thread key={name} name={name} spec={spec.threadSpecs[name]} />
        ))}
      <WfRuns id={spec.id} />
    </>
  )
}

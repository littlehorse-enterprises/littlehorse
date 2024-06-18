'use client'
import { Navigation } from '@/app/(authenticated)/components/Navigation'
import { WfSpec as Spec } from 'littlehorse-client/proto'
import { FC } from 'react'
import { Diagram } from '../../../components/Diagram'
import { Details } from './Details'
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
      <Diagram spec={spec} />
      {Object.keys(spec.threadSpecs)
        .reverse()
        .map(name => (
          <Thread key={name} name={name} spec={spec.threadSpecs[name]} />
        ))}
      <WfRuns {...spec} />
    </>
  )
}

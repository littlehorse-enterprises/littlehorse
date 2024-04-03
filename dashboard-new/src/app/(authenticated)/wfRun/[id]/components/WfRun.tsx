'use client'
import { Navigation } from '@/app/(authenticated)/components/Navigation'
import { Diagram } from '@/app/(authenticated)/wfSpec/[...props]/components/Diagram'
import { FC } from 'react'
import { ReactFlowProvider } from 'reactflow'
import { WfRunResponse } from '../getWfRun'
import { Details } from './Details'

export const WfRun: FC<WfRunResponse> = ({ wfRun, wfSpec }) => {
  return (
    <ReactFlowProvider>
      <Navigation
        href={`/wfSpec/${wfRun.wfSpecId?.name}/${wfRun.wfSpecId?.majorVersion}/${wfRun.wfSpecId?.revision}`}
        title="Go back to WfSpec"
      />
      <Details {...wfRun} />
      <Diagram spec={wfSpec} wfRun={wfRun} />
      <br />
      <br />
      {JSON.stringify(wfSpec)}
      <br />
      <br />
      {JSON.stringify(wfRun)}
    </ReactFlowProvider>
  )
}

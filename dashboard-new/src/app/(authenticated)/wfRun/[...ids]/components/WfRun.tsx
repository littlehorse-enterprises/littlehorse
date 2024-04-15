'use client'
import { Navigation } from '@/app/(authenticated)/components/Navigation'
import { Diagram } from '@/app/(authenticated)/wfSpec/[...props]/components/Diagram'
import { FC } from 'react'
import { ReactFlowProvider } from 'reactflow'
import { WfRunResponse } from '../getWfRun'
import { Details } from './Details'

export const WfRun: FC<WfRunResponse> = ({ wfRun, wfSpec, nodeRuns }) => {
  return (
    <ReactFlowProvider>
      <Navigation
        href={`/wfSpec/${wfRun.wfSpecId?.name}/${wfRun.wfSpecId?.majorVersion}/${wfRun.wfSpecId?.revision}`}
        title="Go back to WfSpec"
      />
      <Details {...wfRun} />
      <Diagram spec={wfSpec} wfRun={wfRun} nodeRuns={nodeRuns} />
    </ReactFlowProvider>
  )
}

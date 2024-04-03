import { Navigation } from '@/app/(authenticated)/components/Navigation'
import { Diagram } from '@/app/(authenticated)/wfSpec/[...props]/components/Diagram'
import { FC, useMemo } from 'react'
import { WfRunResponse } from '../getWfRun'
import { Details } from './Details'

export const WfRun: FC<WfRunResponse> = ({ wfRun, wfSpec }) => {
  const currentThread = useMemo(() => {
    switch (wfRun.status) {
      case 'RUNNING':
        return wfRun.threadRuns[wfRun.greatestThreadrunNumber].threadSpecName
      default:
        return wfSpec.entrypointThreadName
    }
  }, [wfRun.greatestThreadrunNumber, wfRun.status, wfRun.threadRuns, wfSpec.entrypointThreadName])

  return (
    <>
      <Navigation
        href={`/wfSpec/${wfRun.wfSpecId?.name}/${wfRun.wfSpecId?.majorVersion}/${wfRun.wfSpecId?.revision}`}
        title="Go back to WfSpec"
      />
      <Details {...wfRun} />
      <Diagram spec={wfSpec} currentThread={currentThread} />
      <br />
      <br />
      {JSON.stringify(wfSpec)}
      <br />
      <br />
      {JSON.stringify(wfRun)}
    </>
  )
}

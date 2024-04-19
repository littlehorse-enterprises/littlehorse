'use client'
import { Diagram } from '@/app/(authenticated)/(diagram)/components/Diagram'
import { Navigation } from '@/app/(authenticated)/components/Navigation'
import { FC } from 'react'
import { WfRunResponse } from '../getWfRun'
import { Details } from './Details'
import { useModal } from '../../../hooks/useModal'
import { Modals } from '../../../components/Modals'

export const WfRun: FC<WfRunResponse> = ({ wfRun, wfSpec, nodeRuns }) => {
  const {modal, showModal} = useModal()
  return (
    <>
      <Navigation
        href={`/wfSpec/${wfRun.wfSpecId?.name}/${wfRun.wfSpecId?.majorVersion}/${wfRun.wfSpecId?.revision}`}
        title="Go back to WfSpec"
      />
      <Details {...wfRun} />
      <Diagram spec={wfSpec} wfRun={wfRun} nodeRuns={nodeRuns} />

      <Modals />
    </>
  )
}

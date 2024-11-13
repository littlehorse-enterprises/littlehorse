'use client'
import { Navigation } from '@/app/(authenticated)/components/Navigation'
import { WfSpec as Spec } from 'littlehorse-client/proto'
import { FC, useCallback } from 'react'
import { Diagram } from '../../../components/Diagram'
import { Details } from './Details'
import { Thread } from './Thread'
import { WfRuns } from './WfRuns'
import { useModal } from '../../../hooks/useModal'

type WfSpecProps = {
  spec: Spec
}
export const WfSpec: FC<WfSpecProps> = ({ spec }) => {
  const { setModal, setShowModal } = useModal()

  const onClick = useCallback(() => {
    if (!spec) return
    setModal({ type: 'workflowRun', data: { ...spec } })
    setShowModal(true)

  }, [spec, setModal, setShowModal])
  return (
    <>
      <Navigation href="/" title="Go back to WfSpecs" />
      <div className='flex justify-between items-center'>
        <Details status={spec.status} id={spec.id} />
        <button
          className='flex items-center gap-1 p-1 text-white bg-blue-500 rounded-sm'
          onClick={onClick}
        >
          Execute Workflow
        </button>
      </div>
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

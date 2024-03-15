import { WfSpec } from 'littlehorse-client/dist/proto/wf_spec'
import React, { FC } from 'react'
import { TagIcon } from '@heroicons/react/16/solid'
import { Details } from './Details'
import { MetadataStatus } from 'littlehorse-client/dist/proto/common_enums'
import { Variables } from './Variables'
import { Diagram } from './Diagram'
import { Thread } from './Thread'

type WorkflowProps = {
  spec: WfSpec
}
export const Workflow: FC<WorkflowProps> = ({ spec }) => {
  return (
    <div className="mx-auto max-w-screen-xl">
      <Details status={spec.status} id={spec.id} />
      <Diagram />
      {Object.keys(spec.threadSpecs)
        .reverse()
        .map(name => (
          <Thread key={name} name={name} spec={spec.threadSpecs[name]} />
        ))}
      {JSON.stringify(spec)}
    </div>
  )
}

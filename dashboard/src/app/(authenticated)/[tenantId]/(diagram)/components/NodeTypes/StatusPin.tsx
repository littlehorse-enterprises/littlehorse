import { LHStatus } from 'littlehorse-client/proto'

import React, { FC } from 'react'
import { WF_RUN_STATUS } from '../../StatusColor'

export const StatusPin: FC<{ status?: LHStatus }> = ({ status }) => {
  if (!status) return <></>
  const { color, Icon } = WF_RUN_STATUS[status]
  return (
    <div className={`absolute -right-4 -top-4 rounded-full bg-${color}-200 z-10 p-1`}>
      <Icon className={`h-4 w-4 stroke-${color}-500 fill-transparent`} />
    </div>
  )
}

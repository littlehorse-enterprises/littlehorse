import { UserTaskDef as UserTaskDefProto } from 'littlehorse-client/dist/proto/user_tasks'
import React, { FC } from 'react'
import { Details } from './Details'
import { Navigation } from '@/app/(authenticated)/components/Navigation'
import { Fields } from './Fields'

type Props = {
  spec: UserTaskDefProto
}
export const UserTaskDef: FC<Props> = ({ spec }) => {
  return (
    <>
      <Navigation href="/?type=userTaskDef" title="Go back to UserTaskDefs" />
      <Details id={spec} />
      <Fields fields={spec.fields} />
    </>
  )
}

import { FC } from 'react'
import { UserTaskNode } from 'littlehorse-client/dist/proto/wf_spec'
import { getVariable } from '@/app/utils'

export const UserTaskDefDetails: FC<{ userTask: UserTaskNode }> = ({ userTask }) => {
  return (
    <>
      {userTask.userGroup && (
        <div className="flex items-center justify-center">Group: {getVariable(userTask.userGroup)}</div>
      )}

      {userTask.userId && <div className="flex items-center justify-center">User: {getVariable(userTask.userId)}</div>}
    </>
  )
}

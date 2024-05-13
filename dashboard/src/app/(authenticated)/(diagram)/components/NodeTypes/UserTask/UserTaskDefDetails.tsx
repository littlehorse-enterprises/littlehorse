import { FC } from 'react'
import { UserTaskNode } from 'littlehorse-client/dist/proto/wf_spec'
import { getVariable } from '@/app/utils'
import { UserAndGroupAssignmentInfo } from '@/app/(authenticated)/(diagram)/components/NodeTypes/UserTask/UserAndGroupAssignmentInfo'

export const UserTaskDefDetails: FC<{ userTask: UserTaskNode }> = ({ userTask }) => {
  return (
    <UserAndGroupAssignmentInfo userGroup={getVariable(userTask.userGroup)} userId={getVariable(userTask.userId)} />
  )
}

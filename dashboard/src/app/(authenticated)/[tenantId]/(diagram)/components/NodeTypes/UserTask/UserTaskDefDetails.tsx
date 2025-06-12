import { UserAndGroupAssignmentInfo } from '@/app/(authenticated)/[tenantId]/(diagram)/components/NodeTypes/UserTask/UserAndGroupAssignmentInfo'
import { getVariable } from '@/app/utils'
import { UserTaskNode } from 'littlehorse-client/proto'
import { FC } from 'react'
import { UserTaskNotes } from './UserTaskNotes'

export const UserTaskDefDetails: FC<{ userTask: UserTaskNode }> = ({ userTask }) => {
  return (
    <>
      <div className="mb-2 flex flex-wrap gap-2 text-nowrap">
        <UserAndGroupAssignmentInfo userGroup={getVariable(userTask.userGroup)} userId={getVariable(userTask.userId)} />
      </div>
      {userTask.notes && <UserTaskNotes notes={getVariable(userTask.notes)} />}
    </>
  )
}

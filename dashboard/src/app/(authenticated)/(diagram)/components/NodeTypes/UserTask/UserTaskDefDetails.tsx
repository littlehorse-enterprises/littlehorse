import { UserAndGroupAssignmentInfo } from '@/app/(authenticated)/(diagram)/components/NodeTypes/UserTask/UserAndGroupAssignmentInfo'
import { getVariable } from '@/app/utils'
import { UserTaskNode } from 'littlehorse-client'
import { FC } from 'react'
import { UserTaskNotes } from './UserTaskNotes'

export const UserTaskDefDetails: FC<{ userTask: UserTaskNode }> = ({ userTask }) => {
  return (
    <>
      <div className="mb-2 flex gap-2 text-nowrap">
        <UserAndGroupAssignmentInfo userGroup={getVariable(userTask.userGroup)} userId={getVariable(userTask.userId)} />
      </div>
      {userTask.notes && <UserTaskNotes notes={getVariable(userTask.notes)} />}
    </>
  )
}

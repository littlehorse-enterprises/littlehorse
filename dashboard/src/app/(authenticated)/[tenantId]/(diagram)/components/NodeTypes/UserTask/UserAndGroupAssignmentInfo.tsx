import { getVariable } from '@/app/utils/variables'
import { WfRunId } from 'littlehorse-client/proto'
import { FC } from 'react'
interface UserTaskRunDetailsProps {
  userGroup: ReturnType<typeof getVariable>
  userId: ReturnType<typeof getVariable>
}
export const UserAndGroupAssignmentInfo: FC<UserTaskRunDetailsProps> = ({ userGroup, userId }) => {
  return (
    <>
      {userGroup && <div>Group: {userGroup}</div>}
      {userId && <div>User: {userId}</div>}
    </>
  )
}

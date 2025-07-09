import { WfRunId } from 'littlehorse-client/proto'
import { FC } from 'react'
interface UserTaskRunDetailsProps {
  userGroup: string | number | boolean | Buffer | undefined | WfRunId
  userId: string | number | boolean | Buffer | undefined | WfRunId
}
export const UserAndGroupAssignmentInfo: FC<UserTaskRunDetailsProps> = ({ userGroup, userId }) => {
  return (
    <>
      {userGroup && <div>Group: {String(userGroup)}</div>}
      {userId && <div>User: {String(userId)}</div>}
    </>
  )
}

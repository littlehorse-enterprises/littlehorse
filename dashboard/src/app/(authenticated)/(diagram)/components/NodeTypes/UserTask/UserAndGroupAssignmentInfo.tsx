import { FC } from 'react'
interface UserTaskRunDetailsProps {
  userGroup: string | number | boolean | Buffer | undefined
  userId: string | number | boolean | Buffer | undefined
}
export const UserAndGroupAssignmentInfo: FC<UserTaskRunDetailsProps> = ({ userGroup, userId }) => {
  return (
    <>
      {userGroup && <div>Group: {userGroup}</div>}
      {userId && <div>User: {userId}</div>}
    </>
  )
}

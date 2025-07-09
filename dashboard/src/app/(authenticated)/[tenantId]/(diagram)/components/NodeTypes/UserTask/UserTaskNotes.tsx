import { WfRunId } from 'littlehorse-client/proto'
import { FC } from 'react'
interface UserTaskNotesProps {
  notes: string | number | boolean | Buffer | undefined | WfRunId
}
export const UserTaskNotes: FC<UserTaskNotesProps> = ({ notes }) => {
  return (
    <div className="rounded bg-gray-200 p-1">
      <h3 className="mb-1 font-bold">Notes</h3>
      <pre className="overflow-x-auto">{String(notes)}</pre>
    </div>
  )
}

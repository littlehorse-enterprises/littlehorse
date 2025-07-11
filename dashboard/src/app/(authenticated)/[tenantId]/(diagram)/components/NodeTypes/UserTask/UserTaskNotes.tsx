import { FC } from 'react'
interface UserTaskNotesProps {
  notes: string | number | boolean | Buffer | undefined
}
export const UserTaskNotes: FC<UserTaskNotesProps> = ({ notes }) => {
  return (
    <div className="rounded bg-gray-200 p-1">
      <h3 className="mb-1 font-bold">Notes</h3>
      <pre className="overflow-x-auto">{notes}</pre>
    </div>
  )
}

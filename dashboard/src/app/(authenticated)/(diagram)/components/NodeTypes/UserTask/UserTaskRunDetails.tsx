import { getUserTaskRun } from '@/app/(authenticated)/(diagram)/components/NodeTypes/UserTask/getUserTaskRun'
import { UserAndGroupAssignmentInfo } from '@/app/(authenticated)/(diagram)/components/NodeTypes/UserTask/UserAndGroupAssignmentInfo'
import { useModal } from '@/app/(authenticated)/(diagram)/hooks/useModal'
import { useQuery } from '@tanstack/react-query'
import { NodeRun } from 'littlehorse-client/dist/proto/node_run'
import { UserTaskNode } from 'littlehorse-client/dist/proto/wf_spec'
import { EyeIcon } from 'lucide-react'
import { FC, useCallback } from 'react'
import { UserTaskNotes } from './UserTaskNotes'

export const UserTaskRunDetails: FC<{ userTaskNode?: UserTaskNode; nodeRun?: NodeRun }> = ({
  userTaskNode,
  nodeRun,
}) => {
  const { data } = useQuery({
    queryKey: ['userTaskRun', nodeRun],
    queryFn: async () => {
      if (nodeRun?.userTask?.userTaskRunId) return await getUserTaskRun(nodeRun.userTask.userTaskRunId)
      return null
    },
  })

  const { setModal, setShowModal } = useModal()

  const onClick = useCallback(() => {
    if (data) {
      setModal({ type: 'userTaskRun', data, nodeRun, userTaskNode })
      setShowModal(true)
    }
  }, [data, setModal, setShowModal])

  return (
    data && (
      <>
        <div className="mb-2 flex gap-2 text-nowrap">
          <div className="flex-col">
            <div className="mb-2 flex gap-2 ">
              <UserAndGroupAssignmentInfo userGroup={data.userGroup} userId={data.userId} />
            </div>
            <div className="mt-2 flex justify-center">
              <button className="flex items-center gap-1 p-1 text-blue-500 hover:bg-gray-200" onClick={onClick}>
                <EyeIcon className="h-4 w-4" />
                Inspect UserTaskRun
              </button>
            </div>
          </div>
        </div>
        {data.notes && <UserTaskNotes notes={data.notes} />}
      </>
    )
  )
}

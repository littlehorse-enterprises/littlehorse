import { FC, useCallback } from 'react'
import { NodeRun } from 'littlehorse-client/dist/proto/node_run'
import { UserTaskNode } from 'littlehorse-client/dist/proto/wf_spec'
import { useQuery } from '@tanstack/react-query'
import { getUserTaskRun } from '@/app/(authenticated)/(diagram)/components/NodeTypes/UserTask/getUserTaskRun'
import { getVariable } from '@/app/utils'
import { EyeIcon } from '@heroicons/react/24/solid'
import { useModal } from '@/app/(authenticated)/(diagram)/hooks/useModal'

export const UserTaskRunDetails: FC<{ userTask?: UserTaskNode; nodeRun?: NodeRun }> = ({ userTask, nodeRun }) => {
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
      setModal({ type: 'userTaskRun', data })
      setShowModal(true)
    }
  }, [data, setModal, setShowModal])

  return (
    data && (
      <div className="flex-col">
        <div className="mb-2 flex gap-2 ">
          {data.userGroup && <div className="flex items-center justify-center">Group: {data.userGroup}</div>}
          {data.userId && <div className="flex items-center justify-center">User: {data.userId}</div>}
        </div>
        <div className="mt-2 flex justify-center">
          <button className="flex items-center gap-1 p-1 text-blue-500 hover:bg-gray-200" onClick={onClick}>
            <EyeIcon className="h-4 w-4" />
            Inspect UserTaskRun
          </button>
        </div>
      </div>
    )
  )
}

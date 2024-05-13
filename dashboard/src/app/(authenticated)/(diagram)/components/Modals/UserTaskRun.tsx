import { FC, useRef } from 'react'
import { UserTaskModal } from '@/app/(authenticated)/(diagram)/context'
import { useModal } from '@/app/(authenticated)/(diagram)/hooks/useModal'
import { Dialog, DialogPanel, DialogTitle } from '@headlessui/react'
import { DialogBody } from 'next/dist/client/components/react-dev-overlay/internal/components/Dialog'
import { XMarkIcon } from '@heroicons/react/24/outline'
import { getVariableValue } from '@/app/utils'
import { UserTaskRunStatus } from 'littlehorse-client/dist/proto/user_tasks'

export const UserTaskRun: FC<UserTaskModal> = ({ data, nodeRun }) => {
  const { showModal, setShowModal } = useModal()
  const refDiv = useRef(null)
  const assigmentHistory = data.events.filter(e => e.assigned !== undefined)
  const cancellationHistory = data.events.filter(e => e.cancelled !== undefined)
  const resultsToRender = Object.keys(data.results).map(k => ({
    field: k,
    value: getVariableValue(data.results[k]),
  }))

  return (
    <Dialog initialFocus={refDiv} open={showModal} className="relative z-50" onClose={() => setShowModal(false)}>
      <div className="fixed inset-0 bg-black/30" aria-hidden="true" />
      <div className="fixed inset-0 flex items-center justify-center p-4">
        <DialogPanel className="w-1/3 min-w-fit rounded bg-white p-2">
          <DialogTitle className="mb-12">
            <div className="ml-3 mr-2 mt-3 flex h-2 justify-between">
              <div>
                <span className="font-bold">Created On: </span> <span>{data.scheduledTime}</span>
                {data.status === UserTaskRunStatus.DONE && (
                  <div>
                    <span className="font-bold">Completed On: </span> <span> {nodeRun.endTime}</span>
                  </div>
                )}
                {data.status === UserTaskRunStatus.CANCELLED && (
                  <div>
                    <span className="font-bold">Cancelled On: </span> <span> {cancellationHistory[0].time}</span>
                  </div>
                )}
              </div>
              <button className="mr-2 w-5">
                <XMarkIcon onClick={() => setShowModal(false)} />
              </button>
            </div>
          </DialogTitle>
          <DialogBody>
            <hr />
            {resultsToRender.length > 0 && (
              <div className="mt-2">
                <div className="mb-2">
                  <div className="ml-3 h-2 font-bold">Results</div>
                </div>
                <div className="mt-6 flex items-center justify-between p-2">
                  <table className="text-surface min-w-full text-center text-sm font-light dark:text-white">
                    <thead className="border-b border-neutral-200 bg-neutral-300 font-medium dark:border-white/10 dark:text-neutral-800">
                      <tr>
                        <th scope="col" className="px-6 py-4">
                          Field
                        </th>
                        <th scope="col" className="px-6 py-4">
                          Value
                        </th>
                      </tr>
                    </thead>
                    <tbody>
                      {resultsToRender.map((result, index) => (
                        <tr key={index} className="border-b border-neutral-200 dark:border-white/10">
                          <td className="px-6 py-4">{result.field}</td>
                          <td className="px-6 py-4">{result.value}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            )}
            <div className="mt-2">
              <div className="mb-2">
                <div className="ml-3 h-2 font-bold">Assignment History</div>
              </div>
              <div className="mt-6 flex items-center justify-between p-2">
                <table className="text-surface min-w-full text-center text-sm font-light dark:text-white">
                  <thead className="border-b border-neutral-200 bg-neutral-300 font-medium dark:border-white/10 dark:text-neutral-800">
                    <tr>
                      <th scope="col" className="px-6 py-4">
                        Timestamp
                      </th>
                      <th scope="col" className="px-6 py-4">
                        Old User Group
                      </th>
                      <th scope="col" className="px-6 py-4">
                        New User Group
                      </th>
                      <th scope="col" className="px-6 py-4">
                        Old User Id
                      </th>
                      <th scope="col" className="px-6 py-4">
                        New User Id
                      </th>
                    </tr>
                  </thead>
                  <tbody>
                    {assigmentHistory.map((e, index) => (
                      <tr key={index} className="border-b border-neutral-200 dark:border-white/10">
                        <td className="px-6 py-4">{e.time}</td>
                        <td className="px-6 py-4">{e.assigned?.oldUserGroup}</td>
                        <td className="px-6 py-4">{e.assigned?.newUserGroup}</td>
                        <td className="px-6 py-4">{e.assigned?.oldUserId}</td>
                        <td className="px-6 py-4">{e.assigned?.newUserId}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
              {cancellationHistory.length > 0 && (
                <div>
                  <div className="ml-3 mt-6 h-2 font-bold">Cancellation History</div>
                  <div className="mt-6 flex items-center justify-between p-2">
                    <table className="text-surface min-w-full text-center text-sm font-light dark:text-white">
                      <thead className="border-b border-neutral-200 bg-neutral-300 font-medium dark:border-white/10 dark:text-neutral-800">
                        <tr>
                          <th scope="col" className="px-6 py-4">
                            Timestamp
                          </th>
                          <th scope="col" className="px-6 py-4">
                            Message
                          </th>
                        </tr>
                      </thead>
                      <tbody>
                        {cancellationHistory.map((e, index) => (
                          <tr key={index} className="border-b border-neutral-200 dark:border-white/10">
                            <td className="px-6 py-4">{e.time}</td>
                            <td className="px-6 py-4">{e.cancelled?.message}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                </div>
              )}
            </div>
          </DialogBody>
        </DialogPanel>
      </div>
    </Dialog>
  )
}

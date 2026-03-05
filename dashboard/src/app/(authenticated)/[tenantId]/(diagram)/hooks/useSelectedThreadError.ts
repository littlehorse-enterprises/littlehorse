import { ThreadType } from '@/app/(authenticated)/[tenantId]/(diagram)/context'
import { useModal } from './useModal'
import { WfRun } from 'littlehorse-client/proto'
import { useCallback, useMemo } from 'react'

export const useSelectedThreadError = (wfRun: WfRun, selectedThread?: ThreadType) => {
  const { setModal, setShowModal } = useModal()

  const threadError = useMemo(() => {
    if (!selectedThread) {
      const failedThread = wfRun.threadRuns?.find(tr => tr.errorMessage)
      return failedThread ? { errorMessage: failedThread.errorMessage!, threadName: failedThread.threadSpecName ?? '' } : undefined
    }
    const threadRun = wfRun.threadRuns?.find(tr => tr.number === selectedThread.number)
    if (!threadRun?.errorMessage) return undefined
    return { errorMessage: threadRun.errorMessage, threadName: threadRun.threadSpecName ?? selectedThread.name }
  }, [wfRun, selectedThread])

  const onExpandError = useCallback(() => {
    if (!threadError) return
    setModal({ type: 'output', data: { message: threadError.errorMessage, label: 'Error' } })
    setShowModal(true)
  }, [threadError, setModal, setShowModal])

  return { threadError, onExpandError }
}

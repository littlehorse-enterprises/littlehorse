'use client'
import { WfRunId } from 'littlehorse-client/proto'
import useSWR from 'swr'
import { getWfRun } from '../actions/getWfRun'

type Props = {
  wfRunId: WfRunId
  tenantId: string
}

export function useWfRun({ wfRunId, tenantId }: Props) {
  const { data, error, isLoading } = useSWR(
    `wfRun/${tenantId}/${wfRunId.id}/${wfRunId.parentWfRunId?.id}`,
    async () => {
      if (!wfRunId.id) return
      return await getWfRun({ wfRunId, tenantId })
    }
  )

  return {
    wfRunData: data,
    isLoading,
    isError: error,
  }
}

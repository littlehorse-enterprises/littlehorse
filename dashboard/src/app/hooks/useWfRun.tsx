"use client";
import useSWR from "swr";
import { getWfRun, WfRunResponse } from "../actions/getWfRun";
import { useState } from "react";
import { WfRunId } from "littlehorse-client/proto";

type Props = {
    wfRunId: WfRunId
    tenantId: string
}

export function useWfRun({ wfRunId, tenantId }: Props) {
    const [dataCache, setDataCache] = useState<WfRunResponse | null>(null)

    const { error, isLoading } = useSWR(`wfRun/${tenantId}/${wfRunId.id}/${wfRunId.parentWfRunId?.id}`, async () => {
        if (!wfRunId.id) return dataCache
        const data = await getWfRun({ wfRunId, tenantId })
        setDataCache(data)
    })

    return {
        wfRunData: dataCache,
        isLoading,
        isError: error
    }
}

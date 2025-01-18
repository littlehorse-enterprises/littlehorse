"use client";
import useSWR from "swr";
import { getWfRun } from "../actions/getWfRun";

type Props = {
    id: string
    tenantId: string
}

export function useWfRun({ id, tenantId }: Props) {
    const { data, error, isLoading } = useSWR(`wfRun/${tenantId}/${id}`, () => {
        return getWfRun({ ids: [id], tenantId })
    })

    return {
        wfRunData: data,
        isLoading,
        isError: error
    }
}

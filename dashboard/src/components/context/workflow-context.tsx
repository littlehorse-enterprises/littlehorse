"use client"

import { createContext, useContext, ReactNode } from 'react'
import { WfSpec, WfRun, WfSpecId, WfRunId } from 'littlehorse-client/proto'
import { useExecuteRPCWithSWR } from '@/hooks/useExecuteRPCWithSWR'
import { useWfRun } from '@/hooks/useWfRun'
import { WfRunResponse } from '@/actions/getWfRun'

interface WorkflowContextType {
    wfSpec: WfSpec | undefined
    wfRunIds: WfRunId[] | undefined
    wfRun: WfRunResponse | undefined
    isLoading: boolean
    error: Error | undefined
}

const WorkflowContext = createContext<WorkflowContextType | undefined>(undefined)

interface WorkflowProviderProps {
    children: ReactNode
    wfSpecVersion: WfSpecId
    wfRunId: string | undefined
}

export function WorkflowProvider({ children, wfSpecVersion, wfRunId }: WorkflowProviderProps) {
    const { data: wfSpec, error: wfSpecError } = useExecuteRPCWithSWR("getWfSpec", wfSpecVersion)
    const { data: wfRuns, error: wfRunsError } = useExecuteRPCWithSWR("searchWfRun", {
        wfSpecName: wfSpecVersion.name,
        wfSpecMajorVersion: wfSpecVersion.majorVersion,
        wfSpecRevision: wfSpecVersion.revision,
        limit: 100,
        variableFilters: []
    })
    if (!wfRunId)
        wfRunId = wfRuns?.results?.[0]?.id!

    const { wfRunData: wfRun, isLoading: wfRunLoading, isError: wfRunError } = useWfRun({ wfRunId: { id: wfRunId }, tenantId: "default" })
    // const { data: wfRun, error: wfRunError } = useExecuteRPCWithSWR("getWfRun", { id: wfRunId })

    const isLoading = !wfSpec && !wfRuns?.results && !wfRun
    const error = wfSpecError || wfRunsError || wfRunError

    return (
        <WorkflowContext.Provider value={{ wfSpec, wfRunIds: wfRuns?.results, wfRun: wfRun, isLoading, error }}>
            {children}
        </WorkflowContext.Provider>
    )
}

export function useWorkflow() {
    const context = useContext(WorkflowContext)
    if (context === undefined) {
        throw new Error('useWorkflow must be used within a WorkflowProvider')
    }
    return context
} 
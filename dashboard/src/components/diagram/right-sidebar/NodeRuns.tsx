'use client'

import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { useExecuteRPCWithSWR } from "@/hooks/useExecuteRPCWithSWR"
import { WfRun } from "littlehorse-client/proto"
import { Loader2 } from "lucide-react"

interface NodeRunsProps {
  selectedId: string
  wfRun: WfRun
}

export default function NodeRuns({ selectedId, wfRun }: NodeRunsProps) {

  const { data, isLoading } = useExecuteRPCWithSWR("listNodeRuns", {
    wfRunId: wfRun.id,
  })
  const threadSpecName = selectedId.split(":")[1]
  const nodeName = selectedId.split(":")[0]
  const nodeRuns = data?.results ? data.results.filter(nodeRun => nodeRun.nodeName === nodeName && nodeRun.threadSpecName === threadSpecName) : undefined;

  return (
    <div>
      {isLoading && <Loader2 className="animate-spin" />}
      {nodeRuns && nodeRuns.map(nodeRun => (
        <Card key={JSON.stringify(nodeRun.id)}>
          <CardHeader>
            <CardTitle>{nodeRun.nodeName}</CardTitle>
            <CardDescription>{nodeRun.status}</CardDescription>
          </CardHeader>
          <CardContent>
            <p>{nodeRun.status}</p>
            <p>{nodeRun.arrivalTime}</p>
            <p>{nodeRun.endTime}</p>
          </CardContent>
        </Card>
      ))}
    </div>
  )
} 

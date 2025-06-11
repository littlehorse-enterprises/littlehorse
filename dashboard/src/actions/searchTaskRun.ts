"use server"

import { executeRpc } from "./executeRPC"

export interface SearchTaskRunProps {
  taskDefName: string
  tenantId: string
  bookmark?: string
  limit?: number
}

export interface SearchTaskRunResponse {
  results: any[]
  bookmark?: string
}

export const searchTaskRun = async ({
  taskDefName,
  tenantId,
  bookmark,
  limit,
}: SearchTaskRunProps): Promise<SearchTaskRunResponse> => {
  const results = await executeRpc("searchTaskRun", {
    taskDefName,
    limit,
    bookmark: bookmark ? Buffer.from(bookmark, 'base64') : undefined,
  }, tenantId)

  return {
    results: results.results,
    bookmark: results.bookmark?.toString("base64"),
  }
} 
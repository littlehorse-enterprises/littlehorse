"use server";
import { lhClient } from "@/lib/lhClient";

export async function getWfRunsFromIds(wfRunIds: string[], status?: string[]) {
  const client = await lhClient({});
  const wfRuns = await Promise.all(
    wfRunIds.map(async (wfRunId) => {
      return await client.getWfRun({ id: wfRunId });
    })
  );
  if (status) {
    return wfRuns.filter((wfRun) => status.includes(wfRun.status));
  }
  return wfRuns;
}

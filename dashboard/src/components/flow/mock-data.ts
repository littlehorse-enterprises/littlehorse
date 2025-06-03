export const TIME_RANGES = [
  { value: "all", label: "All time", minutes: null },
  { value: "1h", label: "Last hour", minutes: 60 },
  { value: "24h", label: "Last 24 hours", minutes: 1440 },
  { value: "7d", label: "Last 7 days", minutes: 10080 },
  { value: "30d", label: "Last 30 days", minutes: 43200 },
]

export const mockWorkflowRuns = [
  {
    id: "wf-run-1",
    status: "COMPLETED",
    startTime: "2024-01-15T10:30:00Z",
    wfSpecId: { name: "example-workflow", majorVersion: 1, revision: 0 }
  },
  {
    id: "wf-run-2", 
    status: "RUNNING",
    startTime: "2024-01-15T11:00:00Z",
    wfSpecId: { name: "example-workflow", majorVersion: 1, revision: 0 }
  },
  {
    id: "wf-run-3",
    status: "FAILED",
    startTime: "2024-01-15T09:15:00Z",
    wfSpecId: { name: "example-workflow", majorVersion: 1, revision: 0 }
  },
] 
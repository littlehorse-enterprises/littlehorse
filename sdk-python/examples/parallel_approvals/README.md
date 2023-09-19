lhctl run parallel-approvals approvals '[{"userId": "eduwer", "userGroup":"finance"},{"userId": null, "userGroup":"it"}]'

lhctl run parallel-approvals approvals '[{"userId": "maria", "userGroup":"finance"},{"userId": null, "userGroup":"accounting"}]'

lhctl search userTaskRun --userId eduwer
lhctl search userTaskRun --userGroup it

lhctl execute userTaskRun <wfRunId> <userTaskGuid>

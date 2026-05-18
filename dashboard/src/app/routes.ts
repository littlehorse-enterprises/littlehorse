export const routes = {
  appRoot: () => '/',

  tenant: {
    root: (tenantId: string) => `/${tenantId}`,
  },

  wfSpec: {
    base: (name: string) => `/wfSpec/${name}`,
    detail: (name: string, version: string | number) => `/wfSpec/${name}/${version}`,
    detailWithRevision: (name: string, majorVersion: string | number, revision: string | number) =>
      `/wfSpec/${name}/${majorVersion}/${revision}`,
  },

  wfRun: {
    detail: (wfRunUrlSegment: string) => `/wfRun/${wfRunUrlSegment}`,
  },

  structDef: {
    detail: (name: string, version: string | number) => `/structDef/${name}/${version}`,
  },

  taskDef: {
    detail: (name: string) => `/taskDef/${name}`,
  },

  userTaskDef: {
    detail: (name: string, version: string | number) => `/userTaskDef/${name}/${version}`,
    audit: (wfRunId: string, userTaskGuid: string) => `/userTaskDef/audit/${wfRunId}/${userTaskGuid}`,
  },

  externalEventDef: {
    detail: (name: string) => `/externalEventDef/${name}`,
  },

  workflowEventDef: {
    detail: (name: string) => `/workflowEventDef/${name}`,
  },

  search: {
    homeWithType: (type: string) => `/?type=${type}`,
    typeQuery: (type: string) => `?type=${type}`,
  },
} as const

export function withTenant(tenantId: string, tenantRelativePath: string): string {
  if (tenantRelativePath === '') {
    return `/${tenantId}`
  }
  const normalized = tenantRelativePath.startsWith('/') ? tenantRelativePath : `/${tenantRelativePath}`
  return `/${tenantId}${normalized}`
}

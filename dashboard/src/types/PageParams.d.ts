type SearchParam = string | string[] | undefined;

export type PageParams = {
  params: Promise<{ tenantId: string; wfRunId: string; wfSpecVersion: string }>;
  searchParams: Promise<{ wfSpecId: SearchParam }>;
};

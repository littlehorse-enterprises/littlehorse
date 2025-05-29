// todo : this and usePathParams will have to be modified to support any paths. or alternatively, just renamed to be specific to the diagram path
export type PageParams = {
  params: Promise<{
    tenantId: string;
    wfSpecName: string;
    wfSpecVersion: string;
  }>;
  searchParams: Promise<{ wfRunId: string }>;
};

export type PathnameKeys = keyof Awaited<PageParams["params"]>;

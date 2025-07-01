export type PageParams = {
  params: Promise<unknown>
  searchParams: Promise<unknown>
}

export type PathnameKeys = keyof Awaited<PageParams['params']>

// todo : this and usePathParams will have to be modified to support any paths. or alternatively, just renamed to be specific to the diagram path
export type PageParams = {
  params: Promise<unknown>
  searchParams: Promise<unknown>
}

export type PathnameKeys = keyof Awaited<PageParams['params']>

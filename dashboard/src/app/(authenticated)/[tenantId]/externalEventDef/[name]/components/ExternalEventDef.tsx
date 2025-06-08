'use client'
import LinkWithTenant from '@/app/(authenticated)/[tenantId]/components/LinkWithTenant'
import { Navigation } from '@/app/(authenticated)/[tenantId]/components/Navigation'
import { SearchFooter } from '@/app/(authenticated)/[tenantId]/components/SearchFooter'
import { SEARCH_DEFAULT_LIMIT } from '@/app/constants'
import { concatWfRunIds, localDateTimeToUTCIsoString, utcToLocalDateTime } from '@/app/utils'
import { Checkbox } from '@/components/ui/checkbox'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { useInfiniteQuery } from '@tanstack/react-query'
import { ExternalEventDef as ExternalEventDefProto } from 'littlehorse-client/proto'
import { RefreshCwIcon } from 'lucide-react'
import { useParams } from 'next/navigation'
import { FC, Fragment, useState } from 'react'
import { PaginatedExternalEventList, searchExternalEvent } from '../actions/searchExternalEvent'
import { Details } from './Details'

type Props = {
  spec: ExternalEventDefProto
}

export const ExternalEventDef: FC<Props> = ({ spec }) => {
  const [createdAfter, setCreatedAfter] = useState('')
  const [createdBefore, setCreatedBefore] = useState('')
  const [isClaimed, setIsClaimed] = useState<boolean>(true)
  const [limit, setLimit] = useState<number>(SEARCH_DEFAULT_LIMIT)
  const tenantId = useParams().tenantId as string

  const { isPending, data, hasNextPage, fetchNextPage } = useInfiniteQuery({
    queryKey: ['externalEvent', tenantId, createdAfter, limit, createdBefore, isClaimed],
    initialPageParam: undefined,
    getNextPageParam: (lastPage: PaginatedExternalEventList) => lastPage.bookmarkAsString,
    queryFn: async ({ pageParam }) => {
      return await searchExternalEvent({
        tenantId,
        bookmarkAsString: pageParam,
        limit,
        externalEventDefId: { name: spec.id?.name ?? '' },
        isClaimed,
        earliestStart: createdAfter ? localDateTimeToUTCIsoString(createdAfter) : undefined,
        latestStart: createdBefore ? localDateTimeToUTCIsoString(createdBefore) : undefined,
      })
    },
  })

  return (
    <>
      <Navigation href="/?type=ExternalEventDef" title="Go back to ExternalEventDef" />
      <Details spec={spec} />
      <hr className="mt-6" />
      <div className="mb-4 mt-6 flex items-center justify-between">
        <h2 className="text-2xl font-bold">Related External Event&apos;s:</h2>
        <div className="flex items-center space-x-2">
          <Checkbox
            id="isClaimed"
            checked={isClaimed}
            onCheckedChange={() => {
              setIsClaimed(!isClaimed)
            }}
          />
          <Label htmlFor="isClaimed">Is Claimed</Label>
        </div>
      </div>
      <div className="mb-5 flex max-w-fit items-start justify-between">
        <div className="flex items-center justify-between">
          <Label>Created after:</Label>
          <Input
            type="datetime-local"
            value={createdAfter}
            onChange={(e: React.ChangeEvent<HTMLInputElement>) => setCreatedAfter(e.target.value)}
            className="focus:shadow-outline ml-3 w-full appearance-none rounded border px-3 py-2 leading-tight shadow focus:outline-none"
          />
        </div>

        <div className="ml-10 flex items-center justify-between">
          <Label className="block w-1/2 font-bold">Created before:</Label>
          <Input
            type="datetime-local"
            value={createdBefore}
            onChange={(e: React.ChangeEvent<HTMLInputElement>) => setCreatedBefore(e.target.value)}
            className="focus:shadow-outline ml-4 w-full appearance-none rounded border px-3 py-2 leading-tight shadow focus:outline-none"
          />
        </div>
      </div>

      {isPending ? (
        <div className="flex min-h-[360px] items-center justify-center text-center">
          <RefreshCwIcon className="h-8 w-8 animate-spin text-blue-500" />
        </div>
      ) : (
        <div className="flex min-h-[360px] flex-col gap-4">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead scope="col">WfRun Id</TableHead>
                <TableHead scope="col">GUID</TableHead>
                <TableHead scope="col">Triggered Date</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {data?.pages.map((page, i) => (
                <Fragment key={i}>
                  {page.resultsWithDetails.length > 0 ? (
                    page.resultsWithDetails.map(({ externalEvent }) => {
                      return (
                        <TableRow key={externalEvent.id?.guid}>
                          <TableCell>
                            <LinkWithTenant
                              className="py-2 text-blue-500 hover:underline"
                              target="_blank"
                              href={`/wfRun/${concatWfRunIds(externalEvent.id?.wfRunId!)}?threadRunNumber=${externalEvent.threadRunNumber}&nodeRunName=${externalEvent.nodeRunPosition}-${spec.id?.name}-EXTERNAL_EVENT`}
                            >
                              {concatWfRunIds(externalEvent.id?.wfRunId!)}
                            </LinkWithTenant>
                          </TableCell>
                          <TableCell>{externalEvent.id?.guid}</TableCell>

                          <TableCell>
                            {externalEvent.createdAt ? utcToLocalDateTime(externalEvent.createdAt) : 'N/A'}
                          </TableCell>
                        </TableRow>
                      )
                    })
                  ) : (
                    <TableRow>
                      <TableCell colSpan={3} className="text-center">
                        No data
                      </TableCell>
                    </TableRow>
                  )}
                </Fragment>
              ))}
            </TableBody>
          </Table>
        </div>
      )}
      <div className="mt-6">
        <SearchFooter
          currentLimit={limit}
          setLimit={setLimit}
          hasNextPage={hasNextPage}
          fetchNextPage={fetchNextPage}
        />
      </div>
    </>
  )
}

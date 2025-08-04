'use client'
import LinkWithTenant from '@/app/(authenticated)/[tenantId]/components/LinkWithTenant'
import { Navigation } from '@/app/(authenticated)/[tenantId]/components/Navigation'
import { SearchFooter } from '@/app/(authenticated)/[tenantId]/components/SearchFooter'
import { SEARCH_DEFAULT_LIMIT } from '@/app/constants'
import { concatWfRunIds, getVariableValue, localDateTimeToUTCIsoString, utcToLocalDateTime } from '@/app/utils'
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogTrigger,
} from '@/components/ui/alert-dialog'
import { Button } from '@/components/ui/button'
import { Checkbox } from '@/components/ui/checkbox'

import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'

import { useInfiniteQuery } from '@tanstack/react-query'
import { ExternalEventDef as ExternalEventDefProto } from 'littlehorse-client/proto'
import { RefreshCwIcon, Trash2 } from 'lucide-react'
import { useParams, useRouter, useSearchParams } from 'next/navigation'
import { FC, Fragment, useState } from 'react'
import {
  deleteCorrelatedEvent,
  PaginatedCorrelatedEventList,
  PaginatedExternalEventList,
  searchCorrelatedEvent,
  searchExternalEvent,
} from '../actions/searchExternalEvent'
import CreateCorrelatedEventDialog from './CreateCorrelatedEventDialog'
import { Details } from './Details'

type Props = {
  spec: ExternalEventDefProto
}

export const ExternalEventDef: FC<Props> = ({ spec }) => {
  const [createdAfter, setCreatedAfter] = useState('')
  const [createdBefore, setCreatedBefore] = useState('')
  const [isClaimed, setIsClaimed] = useState<boolean>(true)
  const [limit, setLimit] = useState<number>(SEARCH_DEFAULT_LIMIT)

  // Correlated Events state
  const [correlatedCreatedAfter, setCorrelatedCreatedAfter] = useState('')
  const [correlatedCreatedBefore, setCorrelatedCreatedBefore] = useState('')
  const [correlatedLimit, setCorrelatedLimit] = useState<number>(SEARCH_DEFAULT_LIMIT)
  const [hasExternalEvents, setHasExternalEvents] = useState<boolean>(false)
  const [isDeleting, setIsDeleting] = useState(false)

  const tenantId = useParams().tenantId as string
  const router = useRouter()
  const searchParams = useSearchParams()

  // Get current tab from URL or default to 'external-events'
  const currentTab = searchParams.get('tab') || 'external-events'

  const handleTabChange = (value: string) => {
    const params = new URLSearchParams(searchParams.toString())
    params.set('tab', value)
    router.push(`?${params.toString()}`)
  }

  const handleDeleteCorrelatedEvent = async (correlatedEventId: any) => {
    setIsDeleting(true)
    try {
      await deleteCorrelatedEvent({
        tenantId,
        correlatedEventId,
      })
      // Refetch correlatedEvents after successful deletion
      refetchCorrelatedEvents()
    } catch (error) {
      console.error('Failed to delete correlated event:', error)
      // You could add toast notifications here if needed
    } finally {
      setIsDeleting(false)
    }
  }

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

  const {
    isPending: isCorrelatedPending,
    data: correlatedData,
    hasNextPage: hasCorrelatedNextPage,
    fetchNextPage: fetchCorrelatedNextPage,
    refetch: refetchCorrelatedEvents,
  } = useInfiniteQuery({
    queryKey: [
      'correlatedEvent',
      tenantId,
      correlatedCreatedAfter,
      correlatedLimit,
      correlatedCreatedBefore,
      hasExternalEvents,
    ],
    initialPageParam: undefined,
    getNextPageParam: (lastPage: PaginatedCorrelatedEventList) => lastPage.bookmarkAsString,
    queryFn: async ({ pageParam }) => {
      return await searchCorrelatedEvent({
        tenantId,
        bookmarkAsString: pageParam,
        limit: correlatedLimit,
        externalEventDefId: { name: spec.id?.name ?? '' },
        earliestStart: correlatedCreatedAfter ? localDateTimeToUTCIsoString(correlatedCreatedAfter) : undefined,
        latestStart: correlatedCreatedBefore ? localDateTimeToUTCIsoString(correlatedCreatedBefore) : undefined,
        hasExternalEvents: hasExternalEvents,
      })
    },
  })

  return (
    <>
      <Navigation href="/?type=ExternalEventDef" title="Go back to ExternalEventDef" />
      <Details spec={spec} />
      <hr className="mt-6" />
      <div className="mt-6">
        <Tabs value={currentTab} onValueChange={handleTabChange} className="w-full">
          <TabsList className="grid w-full grid-cols-2">
            <TabsTrigger value="external-events">Related External Events</TabsTrigger>
            <TabsTrigger value="correlated-events">Correlated Events</TabsTrigger>
          </TabsList>

          <TabsContent value="external-events">
            <div className="mb-4 mt-6 flex items-center justify-between">
              <h2 className="text-2xl font-bold">Related External Events:</h2>
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
                            if (!externalEvent.id?.wfRunId) return
                            return (
                              <TableRow key={externalEvent.id?.guid}>
                                <TableCell>
                                  <LinkWithTenant
                                    className="py-2 text-blue-500 hover:underline"
                                    target="_blank"
                                    href={`/wfRun/${concatWfRunIds(externalEvent.id.wfRunId)}?threadRunNumber=${externalEvent.threadRunNumber}&nodeRunName=${externalEvent.nodeRunPosition}-${spec.id?.name}-EXTERNAL_EVENT`}
                                  >
                                    {concatWfRunIds(externalEvent.id.wfRunId)}
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
          </TabsContent>

          <TabsContent value="correlated-events">
            <div className="mb-4 mt-6 flex items-center justify-between">
              <h2 className="text-2xl font-bold">Correlated Events:</h2>
              <div className="flex items-center space-x-4">
                <div className="flex items-center space-x-2">
                  <Checkbox
                    id="hasCorrelatedEvents"
                    checked={hasExternalEvents}
                    onCheckedChange={() => {
                      setHasExternalEvents(!hasExternalEvents)
                    }}
                  />
                  <Label htmlFor="hasCorrelatedEvents">Has External Events</Label>
                </div>

                <CreateCorrelatedEventDialog
                  externalEventDef={spec}
                  tenantId={tenantId}
                  onSuccess={refetchCorrelatedEvents}
                />
              </div>
            </div>

            <div className="mb-5 flex max-w-fit items-start justify-between">
              <div className="flex items-center justify-between">
                <Label>Created after:</Label>
                <Input
                  type="datetime-local"
                  value={correlatedCreatedAfter}
                  onChange={(e: React.ChangeEvent<HTMLInputElement>) => setCorrelatedCreatedAfter(e.target.value)}
                  className="focus:shadow-outline ml-3 w-full appearance-none rounded border px-3 py-2 leading-tight shadow focus:outline-none"
                />
              </div>

              <div className="ml-10 flex items-center justify-between">
                <Label className="block w-1/2 font-bold">Created before:</Label>
                <Input
                  type="datetime-local"
                  value={correlatedCreatedBefore}
                  onChange={(e: React.ChangeEvent<HTMLInputElement>) => setCorrelatedCreatedBefore(e.target.value)}
                  className="focus:shadow-outline ml-4 w-full appearance-none rounded border px-3 py-2 leading-tight shadow focus:outline-none"
                />
              </div>
            </div>

            {isCorrelatedPending ? (
              <div className="flex min-h-[360px] items-center justify-center text-center">
                <RefreshCwIcon className="h-8 w-8 animate-spin text-blue-500" />
              </div>
            ) : (
              <div className="flex min-h-[360px] flex-col gap-4">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead scope="col">Correlation Key</TableHead>
                      <TableHead scope="col">Created Date</TableHead>
                      <TableHead scope="col">Content</TableHead>
                      <TableHead scope="col">External Events Count</TableHead>
                      <TableHead scope="col">Actions</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {correlatedData?.pages.map((page, i) => (
                      <Fragment key={i}>
                        {page.resultsWithDetails.length > 0 ? (
                          page.resultsWithDetails.map(({ correlatedEvent }) => {
                            return (
                              <TableRow key={correlatedEvent.id?.key}>
                                <TableCell>{correlatedEvent.id?.key}</TableCell>
                                <TableCell>
                                  {correlatedEvent.createdAt ? utcToLocalDateTime(correlatedEvent.createdAt) : 'N/A'}
                                </TableCell>
                                <TableCell>
                                  <pre className="text-xs">
                                    {correlatedEvent.content
                                      ? JSON.stringify(
                                          JSON.parse(getVariableValue(correlatedEvent.content)?.toString() ?? ''),
                                          null,
                                          2
                                        )
                                      : 'N/A'}
                                  </pre>
                                </TableCell>
                                <TableCell>{correlatedEvent.externalEvents?.length ?? 0}</TableCell>
                                <TableCell>
                                  <AlertDialog>
                                    <AlertDialogTrigger asChild>
                                      <Button variant="destructive" size="sm" disabled={isDeleting}>
                                        <Trash2 className="h-4 w-4" />
                                      </Button>
                                    </AlertDialogTrigger>
                                    <AlertDialogContent>
                                      <AlertDialogHeader>
                                        <AlertDialogTitle>Delete Correlated Event</AlertDialogTitle>
                                        <AlertDialogDescription>
                                          Are you sure you want to delete the correlated event with key "
                                          {correlatedEvent.id?.key}"?
                                        </AlertDialogDescription>
                                      </AlertDialogHeader>
                                      <AlertDialogFooter>
                                        <AlertDialogCancel>Cancel</AlertDialogCancel>
                                        <AlertDialogAction
                                          onClick={async () => {
                                            if (correlatedEvent.id) {
                                              await handleDeleteCorrelatedEvent(correlatedEvent.id)
                                            }
                                          }}
                                        >
                                          Delete
                                        </AlertDialogAction>
                                      </AlertDialogFooter>
                                    </AlertDialogContent>
                                  </AlertDialog>
                                </TableCell>
                              </TableRow>
                            )
                          })
                        ) : (
                          <TableRow>
                            <TableCell colSpan={5} className="text-center">
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
                currentLimit={correlatedLimit}
                setLimit={setCorrelatedLimit}
                hasNextPage={hasCorrelatedNextPage}
                fetchNextPage={fetchCorrelatedNextPage}
              />
            </div>
          </TabsContent>
        </Tabs>
      </div>
    </>
  )
}

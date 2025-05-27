"use client"

import { useState, useEffect } from "react"
import { Search, Filter, Clock, Eye } from "lucide-react"
import { Input } from "@/components/ui/input"
import { Button } from "@/components/ui/button"
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription } from "@/components/ui/dialog"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { useRouter } from "next/navigation"
import { Ids, search, SearchResponse } from "@/actions/searchAction"
import { SEARCH_DEFAULT_LIMIT, SEARCH_ENTITIES, SearchType, SEARCH_LIMITS } from "@/lib/constants"
import useSWRInfinite from "swr/infinite"
import { UserTaskDefId, WfSpecId } from "littlehorse-client/proto"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { LoadMorePagination } from "@/components/ui/load-more-pagination"
import { useSession } from "next-auth/react"
import { AuthStatus } from "@/components/auth"

export default function MetadataSearchPage() {
  const { data: session, status } = useSession()
  console.log("session", session)
  const [activeTab, setActiveTab] = useState<SearchType>("WfSpec")
  const [prefix, setPrefix] = useState("")
  const [selectedItem, setSelectedItem] = useState<any>(null)
  const [isDialogOpen, setIsDialogOpen] = useState(false)
  const [fake, setData] = useState<any[]>([])
  const [limit, setLimit] = useState<number>(SEARCH_DEFAULT_LIMIT)

  const router = useRouter()
  const isLoading = status === "loading"
  const isAuthenticated = status === "authenticated"

  function handleRowClick(name: string, majorVersion?: number, revision?: number) {
    if (activeTab === "WfSpec")
      router.push(`/diagram/${name}?version=${majorVersion}.${revision}`)
    else if (activeTab === "TaskDef")
      router.push(`/taskdef/${name}`)
    else if (activeTab === "UserTaskDef")
      router.push(`/usertaskdef/${name}`)
    else if (activeTab === "ExternalEventDef")
      router.push(`/externaleventdef/${name}`)
    else {
      setSelectedItem(name)
      setIsDialogOpen(true)
    }
  }

  const getKey = (pageIndex: number, previousPageData: SearchResponse | null) => {
    if (!isAuthenticated) return null // Don't fetch if not authenticated
    if (previousPageData && !previousPageData.bookmark) return null // reached the end
    return ['search', activeTab, "default", limit, prefix, previousPageData?.bookmark]
  }

  const { data, error, size, setSize, isLoading: isDataLoading } = useSWRInfinite<SearchResponse>(getKey, async key => {
    const [, type, tenantId, limit, prefix, bookmark] = key
    return search({ type, limit, prefix, bookmark, tenantId })
  })

  // If the user is not authenticated, show a message
  if (!isAuthenticated && !isLoading) {
    return (
      <div className="flex flex-col items-center justify-center h-full p-8">
        <div className="mb-4 text-2xl font-bold">Authentication Required</div>
        <div className="mb-6 text-center text-muted-foreground">
          Please sign in to access the dashboard
        </div>
        <AuthStatus />
      </div>
    )
  }

  // If still loading authentication status, show loading
  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-full">
        <div className="text-xl">Loading...</div>
      </div>
    )
  }

  return (
    <div className="flex-1 overflow-auto">
      <main className="flex-1">
        <div className="container mx-auto py-6">
          <div className="mb-8 flex items-center justify-between">
            <h1 className="text-4xl font-bold">Metadata Search</h1>
            <div className="flex items-center gap-2">
              <div className="relative w-80">
                <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                <Input
                  placeholder="Search metadata..."
                  className="pl-10 pr-4"
                  value={prefix}
                  onChange={(e) => setPrefix(e.target.value)}
                />
              </div>
              <Button variant="outline" onClick={() => router.push('/token-example')}>
                View Token Example
              </Button>
            </div>
          </div>

          <Tabs value={activeTab} onValueChange={(value) => setActiveTab(value as SearchType)} className="w-full">
            <TabsList className="mb-8 border-b w-full justify-start rounded-none bg-transparent p-0">
              {SEARCH_ENTITIES.map((id) => (
                <TabsTrigger
                  key={id}
                  value={id}
                >
                  {id}
                </TabsTrigger>
              ))}
            </TabsList>

            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Name</TableHead>
                  {(activeTab === "WfSpec" || activeTab === "UserTaskDef") && <TableHead>Version</TableHead>}
                  <TableHead>Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {isDataLoading ? (
                  <TableRow>
                    <TableCell colSpan={4} className="text-center py-8 text-muted-foreground">
                      Loading...
                    </TableCell>
                  </TableRow>
                ) : data?.[0]?.results.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={4} className="text-center py-8 text-muted-foreground">
                      No metadata found
                    </TableCell>
                  </TableRow>
                ) : (
                  data?.map((item) => (
                    item.results.map((result: Ids) => (
                      <TableRow
                        key={result.name}
                        className="cursor-pointer hover:bg-muted/50"
                        onClick={() => isOfType<WfSpecId>(activeTab === "WfSpec", result) ? handleRowClick(result.name, result.majorVersion, result.revision) : handleRowClick(result.name)}
                      >
                        <TableCell className="font-medium">{result.name}</TableCell>
                        {isOfType<WfSpecId>(activeTab === "WfSpec", result) && <TableCell>
                          <Badge variant="outline" className="font-mono">
                            {result.majorVersion}.{result.revision}
                          </Badge>
                        </TableCell>
                        }
                        {isOfType<UserTaskDefId>(activeTab === "UserTaskDef", result) && <TableCell>
                          <Badge variant="outline" className="font-mono">
                            {result.version}
                          </Badge>
                        </TableCell>
                        }
                        <TableCell>
                          <Button variant="ghost" size="sm" className="flex items-center gap-1">
                            <Eye className="h-4 w-4" />
                            View
                          </Button>
                        </TableCell>
                      </TableRow>
                    ))
                  ))
                )}
              </TableBody>
            </Table>

            {data && (
              <LoadMorePagination
                limit={limit}
                onLimitChange={setLimit}
                onLoadMore={() => setSize(size + 1)}
                isLoading={isDataLoading}
                limitOptions={SEARCH_LIMITS}
                dataLength={data[0]?.results?.length || 0}
                defaultLimit={SEARCH_DEFAULT_LIMIT}
              />
            )}
          </Tabs>
        </div>
      </main >
    </div >
  )
}

function isOfType<T>(conditional: boolean, obj: unknown): obj is T {
  return conditional
}
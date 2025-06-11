"use client"
import { search, SearchResponse } from "@/actions/search"
import { Pagination } from "@/components/ui/load-more-pagination"
import { SearchType } from "@/types/search"
import { SEARCH_ENTITIES, SEARCH_LIMIT_DEFAULT, SEARCH_LIMITS } from "@/utils/ui/constants"
import { Tabs, TabsList, TabsTrigger } from "@littlehorse-enterprises/ui-library/tabs"
import { useParams } from "next/navigation"
import { useState } from "react"
import useSWRInfinite from "swr/infinite"
import { MetadataTable } from "./metadata-table"
import { SearchHeader } from "./search-header"

export function MetadataSearchClient() {
    const tenantId = useParams().tenantId as string
    const [activeTab, setActiveTab] = useState<SearchType>("WfSpec")
    const [prefix, setPrefix] = useState("")
    const [limit, setLimit] = useState<number>(SEARCH_LIMIT_DEFAULT)

    const getKey = (pageIndex: number, previousPageData: SearchResponse | null) => {
        if (previousPageData && !previousPageData.bookmark) return null // reached the end
        return ['search', activeTab, tenantId, limit, prefix, previousPageData?.bookmark]
    }

    const { data, size, setSize, isLoading: isDataLoading } = useSWRInfinite<SearchResponse>(getKey, async key => {
        const [, type, tenantId, limit, prefix, bookmark] = key
        return search({ type, limit, prefix, bookmark, tenantId })
    })

    return (
        <>
            <SearchHeader prefix={prefix} onPrefixChange={setPrefix} />

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

                <MetadataTable
                    data={data}
                    activeTab={activeTab}
                    isLoading={isDataLoading}
                />

                {data && (
                    <Pagination
                        limit={limit}
                        onLimitChange={setLimit}
                        onLoadMore={() => setSize(size + 1)}
                        isLoading={isDataLoading}
                        limitOptions={SEARCH_LIMITS}
                        hasNextBookmark={!!data[data.length - 1]?.bookmark}
                    />
                )}
            </Tabs>
        </>
    )
}

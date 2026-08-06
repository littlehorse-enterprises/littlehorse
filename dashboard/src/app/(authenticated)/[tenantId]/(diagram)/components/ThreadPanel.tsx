import { Input } from '@/components/ui/input'
import { Pagination, PaginationContent, PaginationItem, PaginationLink } from '@/components/ui/pagination'
import { cn } from '@/components/utils'
import { useWhoAmI } from '@/contexts/WhoAmIContext'
import { LHStatus, WfRun, WfSpec } from 'littlehorse-client/proto'
import { ChevronLeftIcon, ChevronRightIcon, ChevronsLeft, ChevronsRight } from 'lucide-react'
import { useRouter } from 'next/navigation'
import { FC, useCallback, useEffect, useMemo, useState } from 'react'
import { useDiagram } from '../hooks/useDiagram'
import { useReplaceQueryValue } from '../hooks/useReplaceQueryValue'
import { useScrollbar } from '../hooks/useScrollbar'
import { useThreadRunPagination } from '../hooks/useThreadRunPagination'
import { WF_RUN_STATUS } from './Sidebar/Components/StatusColor'

const ThreadTabList: FC<{ children: React.ReactNode; className?: string }> = ({ children, className }) => (
  <div
    role="tablist"
    aria-label="Thread selection"
    className={cn('inline-flex flex-wrap items-center gap-1 rounded-md bg-muted p-1', className)}
  >
    {children}
  </div>
)

const ThreadSpecTab: FC<{
  name: string
  isActive: boolean
  onClick: () => void
}> = ({ name, isActive, onClick }) => (
  <button
    type="button"
    role="tab"
    aria-selected={isActive}
    onClick={onClick}
    className={cn(
      'inline-flex items-center gap-1.5 rounded-sm px-3 py-1.5 text-sm font-medium transition-all',
      'focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2',
      isActive
        ? 'bg-background text-foreground shadow-sm'
        : 'text-muted-foreground hover:bg-background/60 hover:text-foreground'
    )}
  >
    {name}
  </button>
)

const ThreadRunTab: FC<{
  name: string
  number: number
  status: LHStatus
  isActive: boolean
  onClick: () => void
}> = ({ name, number, status, isActive, onClick }) => {
  const { backgroundColor, Icon } = WF_RUN_STATUS[status] ?? WF_RUN_STATUS[LHStatus.STARTING]

  return (
    <button
      type="button"
      role="tab"
      aria-selected={isActive}
      aria-label={`${name} run ${number}, ${LHStatus[status]}`}
      onClick={onClick}
      className={cn(
        'inline-flex shrink-0 items-center gap-2 rounded-sm px-3 py-1.5 text-sm font-medium transition-all',
        'focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2',
        isActive
          ? 'bg-background text-foreground shadow-sm'
          : 'text-muted-foreground hover:bg-background/60 hover:text-foreground'
      )}
    >
      <span className={cn('inline-flex items-center justify-center rounded-full p-0.5', backgroundColor)}>
        <Icon className="h-3 w-3" />
      </span>
      <span>{name}</span>
      <span className="rounded bg-muted px-1.5 py-0.5 font-mono text-xs tabular-nums text-muted-foreground">
        #{number}
      </span>
    </button>
  )
}

export const ThreadPanel: FC<{ spec: WfSpec; wfRun?: WfRun }> = ({ spec, wfRun }) => {
  const { thread, setThread } = useDiagram()
  const { tenantId } = useWhoAmI()
  const [currentPage, setCurrentPage] = useState<number>(1)
  const [pageInput, setPageInput] = useState<string>('1')

  const { threadRuns, isLoading, totalPages } = useThreadRunPagination({
    wfRun,
    page: currentPage,
    tenantId,
  })

  useEffect(() => {
    setCurrentPage(1)
    setPageInput('1')
  }, [wfRun?.id])

  const handlePageChange = useCallback(
    (page: number) => {
      if (page < 1 || page > totalPages || isLoading) return
      setCurrentPage(page)
      setPageInput(page.toString())
    },
    [totalPages, isLoading]
  )

  const handlePageInputChange = useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
    setPageInput(e.target.value)
  }, [])

  const handlePageInputSubmit = useCallback(
    (e: React.KeyboardEvent<HTMLInputElement>) => {
      if (e.key === 'Enter') {
        const page = parseInt(pageInput, 10)
        if (!isNaN(page) && page >= 1 && page <= totalPages) {
          handlePageChange(page)
        } else {
          setPageInput(currentPage.toString())
        }
      }
    },
    [pageInput, totalPages, handlePageChange, currentPage]
  )

  const handlePageInputBlur = useCallback(() => {
    const page = parseInt(pageInput, 10)
    if (!isNaN(page) && page >= 1 && page <= totalPages) {
      handlePageChange(page)
    } else {
      setPageInput(currentPage.toString())
    }
  }, [pageInput, totalPages, handlePageChange, currentPage])

  useEffect(() => {
    setPageInput(currentPage.toString())
  }, [currentPage])

  const threadSpecs = useMemo((): { name: string; number: number }[] => {
    if (!wfRun) {
      return Object.keys(spec.threadSpecs)
        .sort((a, b) => {
          if (a === spec.entrypointThreadName) return -1
          if (b === spec.entrypointThreadName) return 1
          return a.localeCompare(b)
        })
        .map(name => ({ name, number: 0 }))
    }

    return threadRuns.map(threadRun => ({
      name: threadRun.threadSpecName,
      number: threadRun.number,
    }))
  }, [spec, wfRun, threadRuns])

  const threadRunStatusByNumber = useMemo(() => {
    const map = new Map<number, LHStatus>()
    threadRuns.forEach(threadRun => map.set(threadRun.number, threadRun.status))
    return map
  }, [threadRuns])

  const { scroll, itemsRef, containerRef, maxScroll, scrollLeft, scrollRight } = useScrollbar()
  const router = useRouter()
  const replaceQuery = useReplaceQueryValue()

  if (!wfRun) {
    return (
      <ThreadTabList>
        {threadSpecs.map(({ name }) => (
          <ThreadSpecTab
            key={name}
            name={name}
            isActive={name === thread.name}
            onClick={() => {
              setThread({ name, number: 0 })
              router.replace(replaceQuery('thread', name))
            }}
          />
        ))}
      </ThreadTabList>
    )
  }

  return (
    <div className="relative flex items-center gap-2 overflow-hidden">
      {totalPages > 1 && (
        <div className="flex-shrink-0">
          <Pagination className="justify-start">
            <PaginationContent className="gap-0.5">
              <PaginationItem>
                <PaginationLink
                  href="#"
                  onClick={e => {
                    e.preventDefault()
                    handlePageChange(1)
                  }}
                  className={currentPage === 1 ? 'pointer-events-none opacity-50' : ''}
                  size="icon"
                >
                  <ChevronsLeft className="h-4 w-4" />
                </PaginationLink>
              </PaginationItem>

              <PaginationItem>
                <PaginationLink
                  href="#"
                  onClick={e => {
                    e.preventDefault()
                    handlePageChange(currentPage - 1)
                  }}
                  className={currentPage === 1 ? 'pointer-events-none opacity-50' : ''}
                  size="icon"
                >
                  <ChevronLeftIcon className="h-4 w-4" />
                </PaginationLink>
              </PaginationItem>

              <PaginationItem>
                <Input
                  type="number"
                  min={1}
                  max={totalPages}
                  value={pageInput}
                  onChange={handlePageInputChange}
                  onKeyDown={handlePageInputSubmit}
                  onBlur={handlePageInputBlur}
                  className="h-9 w-12 text-center text-sm [-moz-appearance:textfield] [&::-webkit-inner-spin-button]:appearance-none [&::-webkit-outer-spin-button]:appearance-none"
                  disabled={isLoading}
                />
              </PaginationItem>

              <PaginationItem>
                <PaginationLink
                  href="#"
                  onClick={e => {
                    e.preventDefault()
                    handlePageChange(currentPage + 1)
                  }}
                  className={currentPage === totalPages ? 'pointer-events-none opacity-50' : ''}
                  size="icon"
                >
                  <ChevronRightIcon className="h-4 w-4" />
                </PaginationLink>
              </PaginationItem>

              <PaginationItem>
                <PaginationLink
                  href="#"
                  onClick={e => {
                    e.preventDefault()
                    handlePageChange(totalPages)
                  }}
                  className={currentPage === totalPages ? 'pointer-events-none opacity-50' : ''}
                  size="icon"
                >
                  <ChevronsRight className="h-4 w-4" />
                </PaginationLink>
              </PaginationItem>

              {isLoading && (
                <PaginationItem>
                  <span className="ml-1 text-xs text-muted-foreground">Loading...</span>
                </PaginationItem>
              )}
            </PaginationContent>
          </Pagination>
        </div>
      )}

      <div className="relative flex min-w-0 flex-1 items-center overflow-hidden">
        <div
          className={cn(
            "absolute left-0 top-0 z-10 flex after:h-[38px] after:w-[50px] after:bg-gradient-to-r after:from-white after:to-transparent after:content-['']",
            scroll === 0 && 'hidden'
          )}
        >
          <button type="button" className="bg-white" onClick={() => scrollLeft()} aria-label="Scroll left">
            <ChevronLeftIcon className="h-6 w-6" />
          </button>
        </div>
        <div className="flex h-full w-full touch-pan-y items-center text-nowrap" ref={containerRef}>
          <div
            className="duration-[15ms] ease-[cubic-bezier(.05,0,0,1)] flex h-full gap-2 overflow-x-auto will-change-transform"
            style={{ transform: `translateX(${scroll}px)` }}
            ref={itemsRef}
          >
            <ThreadTabList className="flex-nowrap">
              {threadSpecs.map(({ name, number }) => (
                <ThreadRunTab
                  key={`${name}-${number}`}
                  name={name}
                  number={number}
                  status={threadRunStatusByNumber.get(number) ?? LHStatus.STARTING}
                  isActive={name === thread.name && number === thread.number}
                  onClick={() => {
                    setThread({ name, number })
                    router.replace(replaceQuery('threadRunNumber', number.toString()))
                  }}
                />
              ))}
            </ThreadTabList>
          </div>
        </div>
        <div
          className={cn(
            "absolute right-0 top-0 flex before:h-[38px] before:w-[50px] before:bg-gradient-to-l before:from-white before:to-transparent before:content-['']",
            (!maxScroll || -scroll >= maxScroll) && 'hidden'
          )}
        >
          <button type="button" className="bg-white" onClick={() => scrollRight()} aria-label="Scroll right">
            <ChevronRightIcon className="h-6 w-6" />
          </button>
        </div>
      </div>
    </div>
  )
}

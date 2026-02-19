import { Input } from '@/components/ui/input'
import { Pagination, PaginationContent, PaginationItem, PaginationLink } from '@/components/ui/pagination'
import { useWhoAmI } from '@/contexts/WhoAmIContext'
import { WfRun, WfSpec } from 'littlehorse-client/proto'
import { ChevronLeftIcon, ChevronRightIcon, ChevronsLeft, ChevronsRight } from 'lucide-react'
import { useRouter } from 'next/navigation'
import { FC, useCallback, useEffect, useMemo, useState } from 'react'
import { useDiagram } from '../hooks/useDiagram'
import { useReplaceQueryValue } from '../hooks/useReplaceQueryValue'
import { useScrollbar } from '../hooks/useScrollbar'
import { useThreadRunPagination } from '../hooks/useThreadRunPagination'

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

  // Reset to page 1 when wfRun changes
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

  // Sync pageInput when currentPage changes externally
  useEffect(() => {
    setPageInput(currentPage.toString())
  }, [currentPage])

  const threads = useMemo((): { name: string; number: number }[] => {
    if (!wfRun) {
      return Object.keys(spec.threadSpecs).map(name => ({ name, number: 0 }))
    }

    return threadRuns.map(threadRun => ({
      name: threadRun.threadSpecName,
      number: threadRun.number,
    }))
  }, [spec, wfRun, threadRuns])
  const { scroll, itemsRef, containerRef, maxScroll, scrollLeft, scrollRight } = useScrollbar()
  const router = useRouter()
  const replaceQuery = useReplaceQueryValue()

  if (!wfRun) {
    return (
      <div className="mb-2">
        {threads.map(({ name }) => (
          <button
            key={name}
            className={
              'border-[1px] p-2 text-sm shadow ' +
              (name === thread.name ? 'bg-blue-500 text-white' : 'bg-white text-black')
            }
            onClick={() => {
              setThread({ name, number: 0 })
              router.replace(replaceQuery('threadRunNumber', '0'))
            }}
          >
            {name}
          </button>
        ))}
      </div>
    )
  }

  return (
    <div className="mb-2">
      {/* Thread Run Scroll Area with Pagination Controls */}
      <div className="h-full rounded-lg border border-gray-200 bg-white p-3 shadow-sm">
        <div className="relative flex h-full items-center gap-2 overflow-hidden">
          {/* Pagination Controls */}
          {totalPages > 1 && (
            <div className="flex-shrink-0">
              <Pagination className="justify-start">
                <PaginationContent className="gap-0.5">
                  {/* First Page Button */}
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

                  {/* Previous Button */}
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

                  {/* Current Page Number Input */}
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

                  {/* Next Button */}
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

                  {/* Last Page Button */}
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
                      <span className="ml-1 text-xs text-gray-500">Loading...</span>
                    </PaginationItem>
                  )}
                </PaginationContent>
              </Pagination>
            </div>
          )}

          {/* Thread Run Scroll Area */}
          <div className="relative flex h-full flex-1 items-center overflow-hidden">
            <div
              className={`absolute left-0 top-0 z-10 flex after:h-[38px] after:w-[50px] after:bg-gradient-to-r after:from-white after:to-transparent after:content-[''] ${scroll === 0 ? 'hidden' : ''}`}
            >
              <button className="bg-white" onClick={() => scrollLeft()}>
                <ChevronLeftIcon className="h-6 w-6" />
              </button>
            </div>
            <div className="flex h-full w-full touch-pan-y items-center text-nowrap" ref={containerRef}>
              <div
                className="duration-[15ms] ease-[cubic-bezier(.05,0,0,1)] flex h-full gap-2 overflow-x-auto will-change-transform"
                style={{ transform: `translateX(${scroll}px)` }}
                ref={itemsRef}
              >
                {threads.map(({ name, number }) => (
                  <button
                    className={
                      'border-[1px] p-2 text-sm shadow ' +
                      (name === thread.name && number === thread.number
                        ? 'bg-blue-500 text-white'
                        : 'bg-white text-black')
                    }
                    key={`${name}-${number}`}
                    onClick={() => {
                      setThread({ name, number })
                      router.replace(replaceQuery('threadRunNumber', number.toString()))
                    }}
                  >
                    {`${name}-${number}`}
                  </button>
                ))}
              </div>
            </div>
            <div
              className={`absolute right-0 top-0 flex before:h-[38px] before:w-[50px] before:bg-gradient-to-l before:from-white before:to-transparent before:content-[''] ${!maxScroll || -scroll >= maxScroll ? 'hidden' : ''}`}
            >
              <button className="bg-white" onClick={() => scrollRight()}>
                <ChevronRightIcon className="h-6 w-6" />
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}

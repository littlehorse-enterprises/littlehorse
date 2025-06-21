'use client'

import React from 'react'
import {
  Pagination,
  PaginationContent,
  PaginationEllipsis,
  PaginationItem,
  PaginationLink,
  PaginationNext,
  PaginationPrevious,
} from '@/components/ui/pagination'

interface TablePaginationProps {
  currentPage: number
  totalItems: number
  itemsPerPage: number
  onPageChange: (page: number) => void
  showItemCount?: boolean
}

export function TablePagination({
  currentPage,
  totalItems,
  itemsPerPage,
  onPageChange,
  showItemCount = true,
}: TablePaginationProps) {
  const totalPages = Math.ceil(totalItems / itemsPerPage)

  // Handle pagination navigation
  const handlePageChange = (page: number) => {
    if (page < 1 || page > totalPages) return
    onPageChange(page)
  }

  // No pagination needed if there's only one page or no items
  if (totalPages <= 1 || totalItems === 0) {
    return null
  }

  // Generate pagination items
  const generatePaginationItems = () => {
    // For small number of pages, show all page numbers
    if (totalPages <= 5) {
      return Array.from({ length: totalPages }).map((_, idx) => (
        <PaginationItem key={idx}>
          <PaginationLink size="icon" isActive={currentPage === idx + 1} onClick={() => handlePageChange(idx + 1)}>
            {idx + 1}
          </PaginationLink>
        </PaginationItem>
      ))
    }

    // For larger number of pages, show ellipsis
    const items = []

    // Always show first page
    items.push(
      <PaginationItem key={0}>
        <PaginationLink size="icon" isActive={currentPage === 1} onClick={() => handlePageChange(1)}>
          1
        </PaginationLink>
      </PaginationItem>
    )

    // Add ellipsis or second page
    if (currentPage > 3) {
      items.push(<PaginationEllipsis key="ellipsis-1" />)
    } else if (totalPages > 1) {
      items.push(
        <PaginationItem key={1}>
          <PaginationLink size="icon" isActive={currentPage === 2} onClick={() => handlePageChange(2)}>
            2
          </PaginationLink>
        </PaginationItem>
      )
    }

    // Add current page if not at extremes
    if (currentPage > 2 && currentPage < totalPages - 1) {
      items.push(
        <PaginationItem key={currentPage - 1}>
          <PaginationLink size="icon" isActive={true} onClick={() => { }}>
            {currentPage}
          </PaginationLink>
        </PaginationItem>
      )
    }

    // Add ellipsis or second-to-last page
    if (currentPage < totalPages - 2) {
      items.push(<PaginationEllipsis key="ellipsis-2" />)
    } else if (totalPages > 2) {
      items.push(
        <PaginationItem key={totalPages - 2}>
          <PaginationLink size="icon" isActive={currentPage === totalPages - 1} onClick={() => handlePageChange(totalPages - 1)}>
            {totalPages - 1}
          </PaginationLink>
        </PaginationItem>
      )
    }

    // Always show last page
    if (totalPages > 1) {
      items.push(
        <PaginationItem key={totalPages - 1}>
          <PaginationLink size="icon" isActive={currentPage === totalPages} onClick={() => handlePageChange(totalPages)}>
            {totalPages}
          </PaginationLink>
        </PaginationItem>
      )
    }

    return items
  }

  return (
    <div className="mt-4 flex items-center justify-between">
      {showItemCount && (
        <div className="text-muted-foreground text-sm">
          Showing {totalItems > 0 ? (currentPage - 1) * itemsPerPage + 1 : 0} to{' '}
          {Math.min(currentPage * itemsPerPage, totalItems)} of {totalItems} entries
        </div>
      )}
      <Pagination>
        <PaginationContent>
          <PaginationItem>
            <PaginationPrevious
              size="default"
              onClick={() => handlePageChange(currentPage - 1)}
              className={currentPage === 1 ? 'pointer-events-none opacity-50' : ''}
            />
          </PaginationItem>

          {generatePaginationItems()}

          <PaginationItem>
            <PaginationNext
              size="default"
              onClick={() => handlePageChange(currentPage + 1)}
              className={currentPage === totalPages ? 'pointer-events-none opacity-50' : ''}
            />
          </PaginationItem>
        </PaginationContent>
      </Pagination>
    </div>
  )
}

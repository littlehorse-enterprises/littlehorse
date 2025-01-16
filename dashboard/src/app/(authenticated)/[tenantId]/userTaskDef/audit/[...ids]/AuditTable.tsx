'use client'
import { useState } from 'react'
import { Table, TableBody, TableCaption, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import {
  Pagination,
  PaginationContent,
  PaginationItem,
  PaginationLink,
  PaginationNext,
  PaginationPrevious,
} from '@/components/ui/pagination'
import { UserTaskEvent } from 'littlehorse-client/proto'

type AuditTableProps = {
  events: UserTaskEvent[]
}

export function AuditTable({ events }: AuditTableProps) {
  const [currentPage, setCurrentPage] = useState(1)
  const itemsPerPage = 10

  const allEvents = events.filter(event => event.saved !== undefined).reverse()

  const totalPages = Math.ceil(allEvents.length / itemsPerPage)
  const startIndex = (currentPage - 1) * itemsPerPage
  const paginatedEvents = allEvents.slice(startIndex, startIndex + itemsPerPage)

  return (
    <div className="mx-auto w-full max-w-2xl border-blue-300">
      <Table>
        <TableCaption className="mb-2">Audit Log</TableCaption>
        <TableHeader>
          <TableRow>
            <TableHead>Saved At</TableHead>
            <TableHead>Saved By</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {paginatedEvents.map(event => (
            <TableRow key={`${event.time}`}>
              <TableCell>{new Date(event.time ?? 'N/A').toLocaleString()}</TableCell>
              <TableCell> {event.saved?.userId}</TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>

      {totalPages > 1 && (
        <Pagination className="mt-4">
          <PaginationContent>
            <PaginationItem>
              <PaginationPrevious
                href="#"
                onClick={e => {
                  e.preventDefault()
                  if (currentPage > 1) setCurrentPage(currentPage - 1)
                }}
              />
            </PaginationItem>
            {[...Array(totalPages)].map((_, index) => (
              <PaginationItem key={index + 1}>
                <PaginationLink
                  href="#"
                  isActive={currentPage === index + 1}
                  onClick={e => {
                    e.preventDefault()
                    setCurrentPage(index + 1)
                  }}
                >
                  {index + 1}
                </PaginationLink>
              </PaginationItem>
            ))}
            <PaginationItem>
              <PaginationNext
                href="#"
                onClick={e => {
                  e.preventDefault()
                  if (currentPage < totalPages) setCurrentPage(currentPage + 1)
                }}
              />
            </PaginationItem>
          </PaginationContent>
        </Pagination>
      )}
    </div>
  )
}

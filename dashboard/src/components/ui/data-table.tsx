'use client'

import type React from 'react'

import { useState, useCallback } from 'react'
import {
  type ColumnDef,
  type ColumnFiltersState,
  flexRender,
  getCoreRowModel,
  getFilteredRowModel,
  useReactTable,
} from '@tanstack/react-table'

import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@littlehorse-enterprises/ui-library/table'

interface DataTableProps<TData, TValue> {
  columns: ColumnDef<TData, TValue>[]
  data: TData[]
  idField?: keyof TData
  onRowClick?: (row: TData, event: React.MouseEvent) => void
}

export function DataTable<TData, TValue>({
  columns,
  data,
  idField = 'id' as keyof TData,
  onRowClick,
}: DataTableProps<TData, TValue>) {
  const [selectedId, setSelectedId] = useState<string | null>(null)
  const [columnFilters, setColumnFilters] = useState<ColumnFiltersState>([])

  const table = useReactTable({
    data,
    columns,
    state: {
      columnFilters,
    },
    onColumnFiltersChange: setColumnFilters,
    getCoreRowModel: getCoreRowModel(),
    getFilteredRowModel: getFilteredRowModel(),
  })

  const handleRowClick = useCallback(
    (rowId: string, e: React.MouseEvent, rowData: TData) => {
      // Check if the click is coming from a filter or other interactive element

      setSelectedId(rowId)

      // Call the custom row click handler if provided
      if (onRowClick) {
        onRowClick(rowData, e)
      }
    },
    [setSelectedId, onRowClick]
  )

  return (
    <div className="rounded-md border">
      <Table>
        <TableHeader>
          {table.getHeaderGroups().map(headerGroup => (
            <TableRow key={headerGroup.id}>
              {headerGroup.headers.map(header => (
                <TableHead
                  key={header.id}
                  className="h-8 font-medium text-[#656565]"
                  onClick={e => {
                    // Only stop propagation if this is a filter header
                    if (header.column.getCanFilter()) {
                      e.stopPropagation()
                    }
                  }}
                >
                  {header.isPlaceholder ? null : flexRender(header.column.columnDef.header, header.getContext())}
                </TableHead>
              ))}
            </TableRow>
          ))}
        </TableHeader>
        <TableBody>
          {table.getRowModel().rows?.length ? (
            table.getRowModel().rows.map(row => {
              const rowValue = row.original[idField]
              // Handle complex ID objects
              const rowId =
                typeof rowValue === 'object' && rowValue !== null && 'id' in rowValue
                  ? String((rowValue as { id: string }).id)
                  : String(rowValue)
              const isSelected = selectedId === rowId

              return (
                <TableRow
                  key={row.id}
                  className={`cursor-pointer hover:bg-gray-50 ${isSelected ? 'bg-blue-50' : ''}`}
                  onClick={e => handleRowClick(rowId, e, row.original)}
                  data-state={isSelected ? 'selected' : undefined}
                >
                  {row.getVisibleCells().map(cell => (
                    <TableCell key={cell.id} className="py-1">
                      {flexRender(cell.column.columnDef.cell, cell.getContext())}
                    </TableCell>
                  ))}
                </TableRow>
              )
            })
          ) : (
            <TableRow>
              <TableCell colSpan={columns.length} className="h-24 text-center">
                No results.
              </TableCell>
            </TableRow>
          )}
        </TableBody>
      </Table>
    </div>
  )
}

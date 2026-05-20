export type PageItem = number | 'ellipsis'

/** Page numbers with ellipses for large page counts (e.g. 1 … 4 5 6 … 1000). */
export function getVisiblePageNumbers(currentPage: number, totalPages: number): PageItem[] {
  if (totalPages <= 0) return []
  if (totalPages === 1) return [1]
  if (totalPages <= 7) {
    return Array.from({ length: totalPages }, (_, i) => i + 1)
  }

  const pages = new Set<number>([1, totalPages])
  for (let i = currentPage - 1; i <= currentPage + 1; i++) {
    if (i >= 1 && i <= totalPages) pages.add(i)
  }

  const sorted = [...pages].sort((a, b) => a - b)
  const result: PageItem[] = []
  let previous = 0

  for (const page of sorted) {
    if (page - previous > 1) result.push('ellipsis')
    result.push(page)
    previous = page
  }

  return result
}

export function paginateSlice<T>(items: T[], page: number, pageSize: number): T[] {
  const start = (page - 1) * pageSize
  return items.slice(start, start + pageSize)
}

'use client'

import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { cn } from '@/utils/ui/utils'
import { Button } from '@littlehorse-enterprises/ui-library/button'

interface LoadMorePaginationProps {
  limit: number
  onLimitChange: (limit: number) => void
  onLoadMore: () => void
  isLoading: boolean
  limitOptions: readonly number[]
  className?: string
  hasNextBookmark: boolean
}

export function Pagination({
  limit,
  onLimitChange,
  onLoadMore,
  isLoading,
  limitOptions,
  className = '',
  hasNextBookmark,
}: LoadMorePaginationProps) {
  return (
    <div className={cn('mt-4 flex items-center justify-between', className)}>
      <div className="flex items-center gap-2">
        <span className="text-muted-foreground text-sm">Items per load:</span>
        <Select value={limit.toString()} onValueChange={value => onLimitChange(Number(value))}>
          <SelectTrigger className="w-[80px]">
            <SelectValue placeholder={limit} />
          </SelectTrigger>
          <SelectContent>
            {limitOptions.map(option => (
              <SelectItem key={option} value={option.toString()}>
                {option}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>
      {hasNextBookmark && (
        <Button onClick={onLoadMore} disabled={isLoading} className="ml-auto">
          Load More
        </Button>
      )}
    </div>
  )
}

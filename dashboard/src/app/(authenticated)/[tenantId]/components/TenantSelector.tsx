'use client'
import { routes } from '@/app/routes'
import { Input } from '@/components/ui/input'
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover'
import { cn } from '@/components/utils'
import { useWhoAmI } from '@/contexts/WhoAmIContext'
import { useParams, useRouter } from 'next/navigation'
import { FC, useMemo, useState } from 'react'

// Show the search box once the list is large enough that scanning it becomes tedious.
const SEARCH_THRESHOLD = 8

export const TenantSelector: FC = () => {
  const { tenants } = useWhoAmI()
  const tenantId = useParams().tenantId as string
  const router = useRouter()
  const [open, setOpen] = useState(false)
  const [query, setQuery] = useState('')

  const filteredTenants = useMemo(() => {
    const normalized = query.trim().toLowerCase()
    if (!normalized) return tenants
    return tenants.filter(tenant => tenant.toLowerCase().includes(normalized))
  }, [tenants, query])

  const selectTenant = (tenant: string) => {
    setOpen(false)
    router.push(routes.tenant.root(tenant))
  }

  const showSearch = tenants.length > SEARCH_THRESHOLD

  return (
    <Popover
      open={open}
      onOpenChange={nextOpen => {
        setOpen(nextOpen)
        if (!nextOpen) setQuery('')
      }}
    >
      <PopoverTrigger className="inline-flex w-full items-center justify-center gap-x-1.5 rounded bg-white px-3 py-2 text-sm font-semibold text-gray-900 shadow-sm">
        <svg
          xmlns="http://www.w3.org/2000/svg"
          fill="none"
          viewBox="0 0 24 24"
          strokeWidth={1.5}
          stroke="currentColor"
          className="h-4 w-4 text-blue-500"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            d="M6 6.878V6a2.25 2.25 0 0 1 2.25-2.25h7.5A2.25 2.25 0 0 1 18 6v.878m-12 0c.235-.083.487-.128.75-.128h10.5c.263 0 .515.045.75.128m-12 0A2.25 2.25 0 0 0 4.5 9v.878m13.5-3A2.25 2.25 0 0 1 19.5 9v.878m0 0a2.246 2.246 0 0 0-.75-.128H5.25c-.263 0-.515.045-.75.128m15 0A2.25 2.25 0 0 1 21 12v6a2.25 2.25 0 0 1-2.25 2.25H5.25A2.25 2.25 0 0 1 3 18v-6c0-.98.626-1.813 1.5-2.122"
          />
        </svg>
        <p className="break-keep">{tenantId}</p>
      </PopoverTrigger>
      <PopoverContent align="end" className="flex max-h-[--radix-popover-content-available-height] w-72 flex-col p-0">
        <div className="px-3 py-2 text-sm font-bold uppercase">Tenants</div>
        {showSearch && (
          <div className="border-b px-2 pb-2">
            <Input
              autoFocus
              value={query}
              onChange={event => setQuery(event.target.value)}
              placeholder="Search tenants..."
              aria-label="Search tenants"
              className="h-8"
            />
          </div>
        )}
        <div className="max-h-72 overflow-y-auto py-1">
          {filteredTenants.length === 0 ? (
            <p className="px-4 py-2 text-sm text-muted-foreground">No tenants found</p>
          ) : (
            filteredTenants.map(tenant => (
              <button
                key={tenant}
                type="button"
                title={tenant}
                onClick={() => selectTenant(tenant)}
                className={cn(
                  'block w-full break-words px-4 py-2 text-left text-sm hover:bg-gray-100',
                  tenant === tenantId && 'font-semibold text-blue-600'
                )}
              >
                {tenant}
              </button>
            ))
          )}
        </div>
      </PopoverContent>
    </Popover>
  )
}

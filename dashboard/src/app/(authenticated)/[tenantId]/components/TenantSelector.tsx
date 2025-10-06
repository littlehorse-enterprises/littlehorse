'use client'
import { useWhoAmI } from '@/contexts/WhoAmIContext'
import {
  DropdownMenu,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
  DropdownMenuContent,
  DropdownMenuItem,
} from '@/components/ui/dropdown-menu'
import { useParams, useRouter } from 'next/navigation'
import { FC } from 'react'

export const TenantSelector: FC = () => {
  const { tenants } = useWhoAmI()
  const tenantId = useParams().tenantId as string
  const router = useRouter()

  return (
    <DropdownMenu>
      <DropdownMenuTrigger className="inline-flex w-full items-center justify-center gap-x-1.5 rounded bg-white px-3 py-2 text-sm font-semibold text-gray-900 shadow-sm">
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
      </DropdownMenuTrigger>
      <DropdownMenuContent className="absolute right-0 z-10 mt-2 w-56 origin-top-right rounded-md bg-white shadow-lg ring-1 ring-black ring-opacity-5 focus:outline-none">
        <div className="px-2 py-2 text-sm font-bold uppercase">Tenants</div>
        <DropdownMenuSeparator />
        {tenants.map(tenant => (
          <DropdownMenuItem
            key={tenant}
            className="block w-full px-4 py-2 text-left text-sm hover:bg-gray-100"
            onClick={() => router.push(`/${tenant}`)}
          >
            {tenant}
          </DropdownMenuItem>
        ))}
      </DropdownMenuContent>
    </DropdownMenu>
  )
}

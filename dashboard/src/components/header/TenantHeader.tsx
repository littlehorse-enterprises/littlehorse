'use client'

import { handleSignOut } from '@/actions/handleSignOut'
import { useExecuteRPCWithSWR } from '@/hooks/useExecuteRPCWithSWR'
import { Button } from '@littlehorse-enterprises/ui-library/button'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuGroup,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@littlehorse-enterprises/ui-library/dropdown-menu'
import { Check } from 'lucide-react'
import { useSession } from 'next-auth/react'
import { useParams, useRouter } from 'next/navigation'
import { useState } from 'react'

// This exists because the path can't be obtained from the server component layout
export default function TenantHeader() {
  const router = useRouter()
  const { data: session } = useSession()
  const tenantId = useParams().tenantId as string
  const [currentTenant, setCurrentTenant] = useState<string | null>(tenantId)

  const { data } = useExecuteRPCWithSWR('searchTenant', { limit: 12_01_24 })
  const tenants = data?.results

  if (!tenants) return null

  const userName = session?.user?.name || 'User'
  const userEmail = session?.user?.email || ''
  const initials = userName.charAt(0).toUpperCase()

  return (
    <div className="flex items-center">
      <span className="mr-4 text-white">{currentTenant}</span>
      <DropdownMenu>
        <DropdownMenuTrigger asChild>
          <Button className="relative h-10 w-10 rounded-full bg-[#3b81f5] p-0 text-white border-2 white">
            <span className="sr-only">Open user menu</span>
            {initials}
          </Button>
        </DropdownMenuTrigger>
        <DropdownMenuContent className="w-56" align="end" forceMount>
          <DropdownMenuLabel className="font-normal">
            <div className="flex flex-col space-y-1">
              <p className="text-sm leading-none font-medium">{userName}</p>
              <p className="text-muted-foreground text-xs leading-none">{userEmail}</p>
            </div>
          </DropdownMenuLabel>
          <DropdownMenuSeparator />
          <DropdownMenuGroup>
            <DropdownMenuLabel className="text-muted-foreground text-xs">Tenants</DropdownMenuLabel>
            {tenants.map(tenant => (
              <DropdownMenuItem
                key={tenant.id}
                className="cursor-pointer"
                onClick={() => {
                  setCurrentTenant(tenant.id)
                  router.push(`/${tenant.id}`)
                }}
              >
                <span className="flex w-full items-center justify-between">
                  {tenant.id}
                  {currentTenant === tenant.id && <Check className="ml-2 h-4 w-4" />}
                </span>
              </DropdownMenuItem>
            ))}
          </DropdownMenuGroup>
          <DropdownMenuSeparator />
          <DropdownMenuItem onClick={handleSignOut}>Sign Out</DropdownMenuItem>
        </DropdownMenuContent>
      </DropdownMenu>
    </div>
  )
}

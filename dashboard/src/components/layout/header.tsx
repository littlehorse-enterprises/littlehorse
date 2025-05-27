"use client"

import { useState } from "react"
import Image from "next/image"
import Link from "next/link"
import { usePathname } from "next/navigation"
import { LogOut, Check } from "lucide-react"
import { useSession, signOut } from "next-auth/react"
import { LoginButton } from "@/components/auth"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuGroup,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { Button } from "@/components/ui/button"
import { usePathnameParams } from "@/hooks/usePathnameParams"
import { useExecuteRPCWithSWR } from "@/hooks/useExecuteRPCWithSWR"
import { getTenants } from "@/utils/getTenants"

interface HeaderProps {
}

export default function Header() {
  const { tenantId } = usePathnameParams()
  const [currentTenant, setCurrentTenant] = useState(tenantId)
  const { data: session } = useSession()

  const { data } = useExecuteRPCWithSWR(
    "whoami",
    {},
    tenantId
  )

  if (!data) return null

  const tenants = getTenants({ perTenantAcls: data.perTenantAcls, globalAcls: data.globalAcls })


  // If not authenticated, show the login button
  if (!session) {
    return (
      <header className="flex h-16 items-center justify-between border-b border-gray-200 px-4">
        <div className="flex items-center space-x-8">
          <div className="flex items-center">
            <Image
              src="/placeholder.svg?height=24&width=24"
              alt="LittleHorse Logo"
              width={24}
              height={24}
              className="mr-2"
            />
            <span className="text-lg font-bold">LittleHorse</span>
          </div>
        </div>
        <LoginButton className="bg-[#3b81f5] hover:bg-[#3070d8]" />
      </header>
    )
  }

  // Get user info from the session
  const userName = session.user?.name || "User"
  const userEmail = session.user?.email || ""
  // Use first letter of name or email for avatar
  const initials = userName.charAt(0).toUpperCase()

  return (
    <header className="flex h-16 items-center justify-between border-b border-gray-200 px-4">
      <div className="flex items-center space-x-8">
        <div className="flex items-center">
          <Image
            src="/placeholder.svg?height=24&width=24"
            alt="LittleHorse Logo"
            width={24}
            height={24}
            className="mr-2"
          />
          <span className="text-lg font-bold">LittleHorse</span>
        </div>

        <nav className="flex space-x-8">
          <Link
            href="/dashboard"
            className="hover:text-[#3b81f5] text-[#656565]"
          >
            Dashboard
          </Link>
          <Link
            href="/dashboard"
            className="hover:text-[#3b81f5] text-[#656565]"
          >
            Metrics
          </Link>
        </nav>
      </div>

      <div className="flex items-center">
        <span className="mr-4 text-[#656565]">{currentTenant}</span>
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button variant="ghost" className="relative h-10 w-10 rounded-full bg-[#3b81f5] p-0 text-white">
              <span className="sr-only">Open user menu</span>
              {initials}
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent className="w-56" align="end" forceMount>
            <DropdownMenuLabel className="font-normal">
              <div className="flex flex-col space-y-1">
                <p className="text-sm font-medium leading-none">{userName}</p>
                <p className="text-xs leading-none text-muted-foreground">{userEmail}</p>
              </div>
            </DropdownMenuLabel>
            <DropdownMenuSeparator />
            <DropdownMenuGroup>
              <DropdownMenuLabel className="text-xs text-muted-foreground">Tenants</DropdownMenuLabel>
              {tenants.map((tenant) => (
                <DropdownMenuItem key={tenant} className="cursor-pointer" onClick={() => setCurrentTenant(tenant)}>
                  <span className="flex items-center justify-between w-full">
                    {tenant}
                    {currentTenant === tenant && <Check className="h-4 w-4 ml-2" />}
                  </span>
                </DropdownMenuItem>
              ))}
            </DropdownMenuGroup>
            <DropdownMenuSeparator />
            <DropdownMenuItem className="cursor-pointer text-red-600 focus:text-red-600" onClick={() => signOut()}>
              <LogOut className="mr-2 h-4 w-4" />
              <span>Sign out</span>
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      </div>
    </header>
  )
}

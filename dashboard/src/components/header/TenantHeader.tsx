"use client"

import { handleSignOut } from "@/actions/handleSignOut"
import { useExecuteRPCWithSWR } from "@/hooks/useExecuteRPCWithSWR"
import { useTypedParams } from "@/hooks/usePathnameParams"
import { getTenants } from "@/utils/getTenants"
import { Button } from "@littlehorse-enterprises/ui-library/button"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuGroup,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@littlehorse-enterprises/ui-library/dropdown-menu"
import { Check } from "lucide-react"
import { useSession } from "next-auth/react"
import { useRouter } from "next/navigation"
import { useState } from "react"


// this exists because the path can't be obtained from the server component layout
export default function TenantHeader() {
    const router = useRouter()
    const {data: session} = useSession()
    const { tenantId } = useTypedParams()
    const [currentTenant, setCurrentTenant] = useState<string | null>(tenantId)

    const { data: principal } = useExecuteRPCWithSWR("whoami", {})
    if (!principal) return null

    const tenants = getTenants(principal)

    const userName = session?.user?.name || "User"
    const userEmail = session?.user?.email || ""
    const initials = userName.charAt(0).toUpperCase()

    return (
        <div className="flex items-center">
            <span className="mr-4 text-[#656565]">{currentTenant}</span>
            <DropdownMenu>
                <DropdownMenuTrigger asChild>
                    <Button className="relative h-10 w-10 rounded-full bg-[#3b81f5] p-0 text-white">
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
                            <DropdownMenuItem key={tenant} className="cursor-pointer" onClick={() => {
                                setCurrentTenant(tenant)
                                router.push(`/${tenant}`)
                            }}>
                                <span className="flex items-center justify-between w-full">
                                    {tenant}
                                    {currentTenant === tenant && <Check className="h-4 w-4 ml-2" />}
                                </span>
                            </DropdownMenuItem>
                        ))}
                    </DropdownMenuGroup>
                    <DropdownMenuSeparator />
                    <DropdownMenuItem onClick={handleSignOut}>
                      Sign Out
                    </DropdownMenuItem>
                </DropdownMenuContent>
            </DropdownMenu>
        </div>
    )
}

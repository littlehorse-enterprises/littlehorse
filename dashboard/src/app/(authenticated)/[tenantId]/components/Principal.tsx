'use client'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { useWhoAmI } from '@/contexts/WhoAmIContext'
import { signOut } from 'next-auth/react'
import { FC } from 'react'
function classNames(...classes: Array<string | boolean>) {
  return classes.filter(Boolean).join(' ')
}

export const Principal: FC = () => {
  const { user } = useWhoAmI()

  return (
    <DropdownMenu>
      <DropdownMenuTrigger className="inline-flex w-full justify-center gap-x-1.5 px-3 py-2 text-sm font-semibold text-gray-900 shadow-sm">
        <div className="relative inline-flex h-8 w-8 items-center justify-center overflow-hidden rounded-full bg-gray-100 dark:bg-gray-600">
          <span className="font-medium text-gray-600 dark:text-gray-300">{user?.name?.at(0)}</span>
        </div>
      </DropdownMenuTrigger>
      <DropdownMenuContent className="absolute right-0 z-10 mt-2 w-56 origin-top-right rounded-md bg-white shadow-lg ring-1 ring-black ring-opacity-5 focus:outline-none">
        <DropdownMenuLabel>{user?.name}</DropdownMenuLabel>
        <DropdownMenuSeparator />
        <DropdownMenuItem onClick={() => signOut()} className="block w-full px-4 py-2 text-left text-sm">
          Sign out
        </DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  )
}

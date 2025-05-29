import Image from "next/image"
import Link from "next/link"
import TenantHeader from "./TenantHeader"
import { Session } from "next-auth"

interface HeaderProps {
  session: Session | null
}

export default function Header({ session }: HeaderProps) {
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

      <TenantHeader session={session} />
    </header>
  )
}
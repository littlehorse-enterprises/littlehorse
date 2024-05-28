'use client'
import { ChevronLeftIcon } from 'lucide-react'
import Link from 'next/link'
import { FC } from 'react'

type Props = {
  title: string
  href: string
}

export const Navigation: FC<Props> = ({ title, href }) => {
  return (
    <div className="mb-4 flex">
      <Link href={href} className="flex items-center text-blue-500">
        <ChevronLeftIcon className="ml-[-6px] h-6 w-6 stroke-none" />
        {title}
      </Link>
    </div>
  )
}

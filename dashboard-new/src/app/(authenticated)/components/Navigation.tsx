'use client'
import { ChevronLeftIcon } from '@heroicons/react/16/solid'
import Link from 'next/link'
import { FC } from 'react'

type Props = {
  title: string
  href: string
}

export const Navigation: FC<Props> = ({ title, href }) => {
  return (
    <div className="flex mb-4">
      <Link href={href} className="flex items-center text-blue-500">
        <ChevronLeftIcon className="w-6 h-6 stroke-none" />
        {title}
      </Link>
    </div>
  )
}

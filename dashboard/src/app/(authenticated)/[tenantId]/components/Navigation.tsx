'use client'
import { ChevronLeftIcon } from 'lucide-react'
import LinkWithTenant from './LinkWithTenant'
import { FC } from 'react'

type Props = {
  title: string
  href: string
}

export const Navigation: FC<Props> = ({ title, href }) => {
  return (
    <div className="mb-4 flex">
      <LinkWithTenant href={href} className="flex items-center text-blue-500">
        <ChevronLeftIcon className="color-blue-500 ml-[-6px] h-6 w-6" />
        {title}
      </LinkWithTenant>
    </div>
  )
}

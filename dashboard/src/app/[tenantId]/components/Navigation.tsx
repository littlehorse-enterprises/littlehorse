'use client'
import { ChevronLeftIcon } from 'lucide-react'
import Link from 'next/link'
import { FC } from 'react'
import { useParams } from 'next/navigation'

type Props = {
  title: string
  href: string
}

export const Navigation: FC<Props> = ({ title, href }) => {
  const { tenantId } = useParams()

  return (
    <div className="mb-4 flex">
      <Link href={`/${tenantId}/${href}`} className="flex items-center text-blue-500">
        <ChevronLeftIcon className="ml-[-6px] h-6 w-6 stroke-none" />
        {title}
      </Link>
    </div>
  )
}

'use client'

import Image from 'next/image'
import { FC } from 'react'
import LhLogo from '../../../../public/images/littlehorse.svg'

import { Principal } from './Principal'
import { TenantSelector } from './TenantSelector'

export const Header: FC = () => {
  return (
    <nav className="bg-black border-gray-200 dark:bg-gray-900 mb-4">
      <div className="flex flex-wrap items-center justify-between py-4 mx-auto max-w-screen-xl">
        <a href="/" className="flex items-center space-x-1 rtl:space-x-reverse">
          <Image src={LhLogo} alt="littlehorse" width={60} />
          <div className="hidden flex-col text-white text-xl font-bold gap-0 md:flex space-y-[-10px]">
            <span>LITTLE</span>
            <span>HORSE</span>
          </div>
        </a>
        <div className="flex items-center">
          <TenantSelector />
          <Principal />
        </div>
      </div>
    </nav>
  )
}

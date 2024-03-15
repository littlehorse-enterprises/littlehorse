'use client'

import React, { FC } from 'react'
import LhLogo from '../../../../public/images/lh-logo.svg'
import Image from 'next/image'

import { Principal } from './Principal'
import { TenantSelector } from './TenantSelector'

export const Header: FC = () => {
  return (
    <nav className="bg-black border-gray-200 dark:bg-gray-900 mb-4">
      <div className="flex flex-wrap items-center justify-between py-4 mx-auto max-w-screen-xl">
        <a href="/" className="flex items-center space-x-3 rtl:space-x-reverse">
          <Image src={LhLogo} alt="littlehorse" />
        </a>
        <div className="flex items-center">
          <TenantSelector />
          <Principal />
        </div>
      </div>
    </nav>
  )
}

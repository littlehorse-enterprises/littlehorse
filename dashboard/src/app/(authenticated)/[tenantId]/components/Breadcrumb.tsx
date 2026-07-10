'use client'

import {
  Breadcrumb as BreadcrumbRoot,
  BreadcrumbItem as BreadcrumbItemRoot,
  BreadcrumbLink,
  BreadcrumbList,
  BreadcrumbPage,
  BreadcrumbSeparator,
} from '@/components/ui/breadcrumb'
import { cn } from '@/components/utils'
import { FC, Fragment } from 'react'
import LinkWithTenant from './LinkWithTenant'

type BreadcrumbNavItem = {
  label: string
  href?: string
}

type Props = {
  items: BreadcrumbNavItem[]
  className?: string
}

export const Breadcrumb: FC<Props> = ({ items, className }) => (
  <BreadcrumbRoot className={cn('mb-4', className)}>
    <BreadcrumbList>
      {items.map((item, index) => {
        const isLast = index === items.length - 1

        return (
          <Fragment key={`${item.label}-${index}`}>
            {index > 0 && <BreadcrumbSeparator />}
            <BreadcrumbItemRoot>
              {item.href && !isLast ? (
                <BreadcrumbLink asChild>
                  <LinkWithTenant href={item.href}>{item.label}</LinkWithTenant>
                </BreadcrumbLink>
              ) : (
                <BreadcrumbPage>{item.label}</BreadcrumbPage>
              )}
            </BreadcrumbItemRoot>
          </Fragment>
        )
      })}
    </BreadcrumbList>
  </BreadcrumbRoot>
)

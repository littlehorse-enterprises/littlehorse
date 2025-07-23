import LinkWithTenant from './LinkWithTenant'

import { Fragment } from 'react'

import { Separator } from '@/components/ui/separator'

export const SelectionLink = ({
  href,
  key,
  children,
  ...props
}: { href: string | undefined } & Omit<React.ComponentProps<typeof LinkWithTenant>, 'href'>) => {
  return href !== undefined ? (
    <Fragment key={key}>
      <LinkWithTenant
        className="flex items-center gap-3 rounded-md px-2 py-2 hover:bg-gray-100"
        href={href}
        linkStyle={href !== undefined}
        {...props}
      >
        {children}
      </LinkWithTenant>
      <Separator />
    </Fragment>
  ) : (
    <Fragment key={key}>
      <div className="flex items-center gap-3 rounded-md px-2 py-2 hover:bg-gray-100">{children}</div>
      <Separator />
    </Fragment>
  )
}

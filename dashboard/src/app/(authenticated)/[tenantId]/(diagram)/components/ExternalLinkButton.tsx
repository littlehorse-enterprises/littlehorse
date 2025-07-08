import { Button } from '@/components/ui/button'
import LinkWithTenant from '@/app/(authenticated)/[tenantId]/components/LinkWithTenant'
import { ExternalLinkIcon } from 'lucide-react'
import { ComponentProps } from 'react'
import { cn } from '@/components/utils'

type ExternalLinkButtonProps = {
  href: string
  label: string
  target?: string
} & ComponentProps<typeof Button>

export const ExternalLinkButton = ({ href, label, target, className, ...props }: ExternalLinkButtonProps) => (
  <Button variant="link" className={cn('h-5 w-fit p-0 text-xs', className)} {...props}>
    <LinkWithTenant href={href} target={target} className="flex gap-1">
      {label} <ExternalLinkIcon className="h-4 w-4" />
    </LinkWithTenant>
  </Button>
)

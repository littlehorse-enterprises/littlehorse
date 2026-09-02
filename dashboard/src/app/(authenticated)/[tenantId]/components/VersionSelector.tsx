import { Select, SelectContent, SelectTrigger, SelectValue } from '@/components/ui/select'
import { cn } from '@/components/utils'
import { TagIcon } from 'lucide-react'
import { FC } from 'react'
import LinkWithTenant from './LinkWithTenant'

type Props = {
  path: string
  currentVersion: string
  versions: string[]
  loadVersions: () => void
  hideLabel?: boolean
  versionPrefix?: string
  compact?: boolean
}

const formatVersion = (version: string, prefix?: string) => `${prefix ?? ''}${version}`

export const VersionSelector: FC<Props> = ({
  path,
  currentVersion,
  versions,
  loadVersions,
  hideLabel = false,
  versionPrefix,
  compact = false,
}) => {
  return (
    <div className="flex items-center gap-2">
      {!hideLabel && <span className="text-sm font-medium text-gray-900">Version:</span>}
      <Select defaultValue={currentVersion} onOpenChange={loadVersions}>
        <SelectTrigger
          className={cn(
            'w-auto',
            compact &&
              'h-7 gap-1.5 rounded-full border-muted-foreground/20 bg-muted/40 px-2.5 py-0 text-xs font-medium shadow-none'
          )}
        >
          <SelectValue>
            <div className="flex items-center gap-1.5">
              <TagIcon className={cn('text-muted-foreground', compact ? 'h-3.5 w-3.5' : 'h-4 w-4')} />
              {formatVersion(currentVersion, versionPrefix)}
            </div>
          </SelectValue>
        </SelectTrigger>
        <SelectContent>
          {[...versions].reverse().map(version => (
            <LinkWithTenant
              key={version}
              href={`${path}/${version}`}
              className="relative block cursor-pointer select-none p-2 hover:bg-slate-300"
            >
              {formatVersion(version, versionPrefix)}
            </LinkWithTenant>
          ))}
        </SelectContent>
      </Select>
    </div>
  )
}

import { TagIcon } from 'lucide-react'
import { FC } from 'react'
import LinkWithTenant from './LinkWithTenant'
import { Select, SelectContent, SelectTrigger, SelectValue } from '@/components/ui/select'

type Props = {
  path: string
  currentVersion: string
  versions: string[]
  loadVersions: () => void
}

export const VersionSelector: FC<Props> = ({ path, currentVersion, versions, loadVersions }) => {
  return (
    <div className="flex items-center gap-2">
      <span className="text-sm font-medium text-gray-900">Version:</span>
      <Select defaultValue={currentVersion} onOpenChange={loadVersions}>
        <SelectTrigger className="w-auto">
          <SelectValue>
            <div className="flex items-center gap-2">
              <TagIcon className="h-5 w-5" />
              {currentVersion}
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
              {version}
            </LinkWithTenant>
          ))}
        </SelectContent>
      </Select>
    </div>
  )
}

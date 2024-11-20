import { Listbox } from '@headlessui/react'
import { TagIcon } from 'lucide-react'
import Link from 'next/link'
import { FC } from 'react'
import LinkWithTenant from './LinkWithTenant'

type Props = {
  path: string
  currentVersion: string
  versions: string[]
  loadVersions: () => void
}

export const VersionSelector: FC<Props> = ({ path, currentVersion, versions, loadVersions }) => {
  return (
    <Listbox>
      <div className="flex">
        <Listbox.Label className="block text-sm font-medium leading-6 text-gray-900">Version: </Listbox.Label>
        <div className="relative">
          <Listbox.Button onClick={loadVersions} className="ml-2 flex gap-2 rounded border-2 border-slate-100 px-2">
            <TagIcon className="h-5 w-5" />
            {currentVersion}
          </Listbox.Button>
          <Listbox.Options className="absolute right-0 z-10 mt-1 max-h-60 overflow-auto rounded-md bg-white py-1 text-base shadow-lg ring-1 ring-black/5 focus:outline-none sm:text-sm">
            {versions.map(version => (
              <Listbox.Option
                className="relative block cursor-pointer select-none p-2 hover:bg-slate-300"
                key={version}
                value={version}
                as={LinkWithTenant}
                href={`${path}/${version}`}
              >
                {version}
              </Listbox.Option>
            ))}
          </Listbox.Options>
        </div>
      </div>
    </Listbox>
  )
}

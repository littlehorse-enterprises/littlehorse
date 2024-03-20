import { useWhoAmI } from '@/contexts/WhoAmIContext'
import { Listbox } from '@headlessui/react'
import { TagIcon } from '@heroicons/react/16/solid'
import { WfSpecId } from 'littlehorse-client/dist/proto/object_id'
import Link from 'next/link'
import { useParams } from 'next/navigation'
import { FC, useCallback, useState } from 'react'
import { getWfSpecVersions } from '../getVersions'

export const VersionSelector: FC<{ wfSpecId?: WfSpecId }> = ({ wfSpecId }) => {
  const [versions, setVersions] = useState<string[]>([])
  const { tenantId } = useWhoAmI()
  const { name, majorVersion, revision } = wfSpecId!
  const { props } = useParams()

  const loadVersions = useCallback(async () => {
    const { versions } = await getWfSpecVersions({ name, tenantId })
    setVersions(versions)
  }, [name, tenantId])

  return (
    <Listbox>
      <div className="flex">
        <Listbox.Label className="block text-sm font-medium leading-6 text-gray-900">Version: </Listbox.Label>
        <div className="relative">
          <Listbox.Button onClick={loadVersions} className="ml-2 flex gap-2 rounded border-2 border-slate-100 px-2">
            <TagIcon className="h-5 w-5" />
            {majorVersion}.{revision}
          </Listbox.Button>
          <Listbox.Options className="absolute right-0 mt-1 max-h-60 overflow-auto rounded-md bg-white py-1 text-base shadow-lg ring-1 ring-black/5 focus:outline-none sm:text-sm">
            {versions.map(version => (
              <Listbox.Option
                className="relative block cursor-pointer select-none p-2 hover:bg-slate-300"
                key={version}
                value={version}
                as={Link}
                href={`/wfSpec/${props[0]}/${version}`}
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

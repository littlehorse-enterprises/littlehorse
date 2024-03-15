'use client'
import { Listbox } from '@headlessui/react'
import { TagIcon } from '@heroicons/react/24/outline'
import { WfSpecId } from 'littlehorse-client/dist/proto/object_id'
import { WfSpec } from 'littlehorse-client/dist/proto/wf_spec'
import React, { FC, useCallback, useState } from 'react'
import { getWfSpecVersions } from '../getVersions'
import { useWhoAmI } from '@/contexts/WhoAmIContext'

type DetailsProps = Pick<WfSpec, 'id' | 'status'>

const statusColors: { [key in WfSpec['status']]: string } = {
  ARCHIVED: 'bg-gray-200',
  ACTIVE: 'bg-blue-200',
  TERMINATING: 'bg-yellow-200',
  UNRECOGNIZED: 'bg-red-200',
}

export const Details: FC<DetailsProps> = ({ id, status }) => {
  return (
    <div className="mb-4">
      <span>Workflow</span>
      <h1 className="block font-bold text-2xl">{id?.name}</h1>
      <div className="text-sm text-gray-500">
        <div className="inline-flex mr-2">
          Version:
          {id && <VersionSelector wfSpecId={id} />}
          {/* <span className="flex rounded items-center ml-2 px-2">
            <TagIcon className="w-4 text-transparent h-4 mr-2 stroke-black" />
            {`${id?.majorVersion}.${id?.revision}`}
          </span> */}
        </div>
        <div className="inline-flex">
          Status: <span className={`rounded ml-2 px-2 ${statusColors[status]}`}>{`${status}`}</span>
        </div>
      </div>
    </div>
  )
}

const VersionSelector: FC<{ wfSpecId: WfSpecId }> = ({ wfSpecId: { name, majorVersion, revision } }) => {
  const [versions, setVersions] = useState<string[]>([])
  const { tenantId } = useWhoAmI()

  const loadVersions = useCallback(async () => {
    const result = await getWfSpecVersions({ name, tenantId })
    console.log(result)
    setVersions(result)
  }, [name, tenantId])

  return (
    <Listbox value={`${majorVersion}.${revision}`}>
      <Listbox.Button onClick={loadVersions}>
        {majorVersion}.{revision}
      </Listbox.Button>
      <Listbox.Options className="absolute mt-1 max-h-60 overflow-auto rounded-md bg-white py-1 text-base shadow-lg ring-1 ring-black/5 focus:outline-none sm:text-sm">
        {versions.map(version => (
          <Listbox.Option className="relative cursor-default select-none py-2 pl-10 pr-4" key={version} value={version}>
            {version}
          </Listbox.Option>
        ))}
      </Listbox.Options>
    </Listbox>
  )
}

'use client'
import { Disclosure } from '@headlessui/react'
import { ChevronUpIcon } from '@heroicons/react/20/solid'
import { ThreadSpec } from 'littlehorse-client/dist/proto/wf_spec'
import { FC } from 'react'
import { Mutations } from './Mutations'
import { Variables } from './Variables'

type Props = {
  name: string
  spec: ThreadSpec
}
export const Thread: FC<Props> = ({ name, spec }) => {
  return (
    <div className="mb-4 border-2 rounded border-slate-100 p-2">
      <Disclosure>
        {({ open }) => (
          <>
            <Disclosure.Button className="flex items-center w-full justify-between">
              <h2 className="text-xl">Thread: {name}</h2>
              <ChevronUpIcon className={`${open ? 'rotate-180 transform' : ''} h-6 w-6`} />
            </Disclosure.Button>
            <Disclosure.Panel className="flex gap-4">
              <Variables variableDefs={spec.variableDefs} />
              <Mutations nodes={spec.nodes} />
            </Disclosure.Panel>
          </>
        )}
      </Disclosure>
    </div>
  )
}

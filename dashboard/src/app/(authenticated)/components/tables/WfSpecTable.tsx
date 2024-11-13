import { FC, Fragment, useMemo } from 'react'
import { SearchResultProps } from '.'
import { WfSpecId } from 'littlehorse-client/proto'

import { TagIcon } from 'lucide-react'
import { Separator } from '@/components/ui/separator'
import { useRouter } from 'next/navigation'

export const WfSpecTable: FC<SearchResultProps> = ({ pages = [] }) => {
  const router = useRouter()

  if (pages.length === 0) {
    return <div className="flex min-h-[360px] items-center justify-center text-center italic">No WfSpecs</div>
  }

  const wfSpecs = useMemo(() => {
    const specMap = new Map<string, wfSpecData>()

    pages
      .flatMap(page => page.results)
      .reverse()
      .forEach(({ name, majorVersion, revision }: WfSpecId) => {
        if (!specMap.has(name)) {
          specMap.set(name, { name, latestVersion: `${majorVersion}.${revision}` })
        }
      })

    return Array.from(specMap.values())
  }, [pages])

  return (
    <div className="py-4">
      <div className="flex max-h-[600px] flex-col overflow-auto">
        {wfSpecs.map(wfSpec => (
          <Fragment key={wfSpec.name}>
            <button
              className="flex items-center gap-3 rounded-md px-2 py-2 hover:bg-gray-100"
              key={wfSpec.name}
              onClick={() => router.push(`/wfSpec/${wfSpec.name}/${wfSpec.latestVersion}`)}
            >
              <p>{wfSpec.name}</p>
              <div className="flex items-center gap-2 rounded bg-blue-200 px-2 font-mono text-sm text-gray-500">
                <TagIcon className="h-4 w-4 fill-none stroke-gray-500 stroke-1" />
                Latest: v{wfSpec.latestVersion}
              </div>
            </button>
            <Separator />
          </Fragment>
        ))}
      </div>
    </div>
  )
}
type wfSpecData = {
  name: string
  latestVersion: string
}

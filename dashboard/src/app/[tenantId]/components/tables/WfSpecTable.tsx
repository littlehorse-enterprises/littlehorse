import { getLatestWfSpecs } from '@/app/actions/getLatestWfSpec'
import { Separator } from '@/components/ui/separator'
import { useWhoAmI } from '@/contexts/WhoAmIContext'
import { WfSpecData } from '@/types'
import { TagIcon } from 'lucide-react'
import { useRouter } from 'next/navigation'
import { FC, Fragment, useEffect, useState } from 'react'
import { SearchResultProps } from '.'

export const WfSpecTable: FC<SearchResultProps> = ({ pages = [] }) => {
  const router = useRouter()
  const [wfSpecs, setWfSpecs] = useState<WfSpecData[]>([])
  const { tenantId } = useWhoAmI()

  useEffect(() => {
    if (!tenantId) return
    const wfSpecNames = pages.flatMap(page => page.results).map(wfSpec => wfSpec.name)
    getLatestWfSpecs(tenantId, wfSpecNames).then(setWfSpecs)
  }, [pages, tenantId])

  if (pages.length === 0) {
    return <div className="flex min-h-[360px] items-center justify-center text-center italic">No WfSpecs</div>
  }

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

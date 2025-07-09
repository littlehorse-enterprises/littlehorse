import { getLatestWfSpecs } from '@/app/actions/getLatestWfSpec'
import { WfSpecData } from '@/types'
import { useParams, useRouter } from 'next/navigation'
import { FC, useEffect, useState } from 'react'
import { SearchResultProps } from '.'
import { SelectionLink } from '../SelectionLink'

import VersionTag from '../VersionTag'
export const WfSpecTable: FC<SearchResultProps> = ({ pages = [] }) => {
  const router = useRouter()
  const tenantId = useParams().tenantId as string
  const [wfSpecs, setWfSpecs] = useState<WfSpecData[]>([])

  useEffect(() => {
    const wfSpecNames = pages.flatMap(page => page.results).map(wfSpec => wfSpec.name)
    getLatestWfSpecs(tenantId, wfSpecNames).then(setWfSpecs)
  }, [pages, tenantId])

  if (pages.every(page => page.results.length === 0)) {
    return <div className="flex min-h-[360px] items-center justify-center text-center italic">No WfSpecs</div>
  }

  return (
    <div className="py-4">
      <div className="flex max-h-[600px] flex-col overflow-auto">
        {wfSpecs.map(wfSpec => (
          <SelectionLink key={wfSpec.name} href={`/wfSpec/${wfSpec.name}/${wfSpec.latestVersion}`}>
            <p className="group">{wfSpec.name}</p>
            <VersionTag label={`Latest: v${wfSpec.latestVersion}`} />
          </SelectionLink>
        ))}
      </div>
    </div>
  )
}

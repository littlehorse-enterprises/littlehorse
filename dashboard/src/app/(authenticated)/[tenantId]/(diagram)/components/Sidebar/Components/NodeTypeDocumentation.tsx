import { ExternalLink } from 'lucide-react'
import { FC } from 'react'

export const NodeTypeDocumentation: FC<{ nodeType?: string; showNodeRun?: boolean; className?: string }> = ({
  nodeType,
  showNodeRun,
  className,
}) => {
  const type = nodeType?.toLowerCase()
  if (!type) return null
  const handleInfoNodeTypeClick = () => {
    const type = nodeType?.toLowerCase()
    if (!type) return
    const suffix = showNodeRun ? 'run' : 'node'
    window.open(
      `https://littlehorse.io/docs/server/api#${encodeURIComponent(type)}${suffix}`,
      '_blank',
      'noopener,noreferrer'
    )
  }
  return (
    <div className="flex cursor-pointer items-center rounded hover:bg-gray-100" onClick={handleInfoNodeTypeClick}>
      <div className={className}>
        {' '}
        Node {type} {showNodeRun && 'run'}
      </div>
      <div className="ml-2">
        <ExternalLink size={16} onClick={handleInfoNodeTypeClick} color={'black'} className="mt-1" />
      </div>
    </div>
  )
}

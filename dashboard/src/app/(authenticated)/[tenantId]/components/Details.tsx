import { TagIcon } from 'lucide-react'
import { FC } from 'react'


export type DetailsProps = {
  itemHeader: string
  header: string
  version?: number[]
  status?: string
  description?: Record<string, string | React.ReactNode>
}

export const Details: FC<DetailsProps> = ({ itemHeader, header, version, status, description }) => {
  return (
    <div>
      <div className="mb-4">
        <span className="italic">{itemHeader}</span>
        <h1 className="block text-2xl font-bold">{header}</h1>
        <div className="flex flex-row gap-2 text-sm text-gray-500">
          {version && version.length > 1 ? (
            <div className="flex items-center gap-2">
              <TagIcon className="h-5 w-5" />
              {version[0]}
            </div>
          ) : null}
        </div>
        {description &&
          Object.entries(description).map(([key, value]) => (
            <div key={key}>
              <b>{key}</b>: {value}
            </div>
          ))}
        {status && <div className="italic text-gray-400">{status}</div>}
      </div>
    </div>
  )
}

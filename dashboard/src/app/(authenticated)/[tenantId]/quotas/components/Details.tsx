'use client'
import { FC } from 'react'

type DetailsProps = {
  tenantId: string
  limitRps?: number
  limitsKnown: boolean
}

export const Details: FC<DetailsProps> = ({ tenantId, limitRps, limitsKnown }) => (
  <div className="mb-4 space-y-1">
    <span className="italic">Quota Usage</span>
    <h1 className="block text-2xl font-bold">{tenantId}</h1>
    {limitsKnown && (
      <div className="mt-2 flex flex-wrap items-center gap-2 text-sm text-gray-600">
        <span className="font-semibold text-gray-700">Write requests per second:</span>
        {limitRps === undefined ? (
          <span className="font-mono text-gray-400">No quota configured</span>
        ) : (
          <span className="font-mono">{limitRps}</span>
        )}
      </div>
    )}
  </div>
)

'use client'
import { FC } from 'react'
import { QuotaScope } from '../actions/getApplicableQuota'

type DetailsProps = {
  tenantId: string
  scope: QuotaScope
  principalId?: string
  limitRps?: number
}

export const Details: FC<DetailsProps> = ({ tenantId, scope, principalId, limitRps }) => (
  <div className="mb-4 space-y-1">
    <span className="italic">Quota Usage</span>
    <h1 className="block text-2xl font-bold">{tenantId}</h1>
    <div className="mt-2 flex flex-wrap items-center gap-2 text-sm text-gray-600">
      <span className="font-semibold text-gray-700">Quota:</span>
      {scope === 'none' ? (
        <span className="font-mono text-gray-400">None configured</span>
      ) : (
        <>
          <span>{scope === 'principal' ? `Principal ${principalId}` : 'Tenant-wide'}</span>
          <span className="font-mono">{limitRps} writes/s</span>
        </>
      )}
    </div>
  </div>
)

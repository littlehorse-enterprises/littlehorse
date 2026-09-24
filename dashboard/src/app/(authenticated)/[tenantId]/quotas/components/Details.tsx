'use client'
import { FC } from 'react'

type DetailsProps = {
  tenantId: string
}

export const Details: FC<DetailsProps> = ({ tenantId }) => (
  <div className="mb-4 space-y-1">
    <span className="italic">Quota Usage</span>
    <h1 className="block text-2xl font-bold">{tenantId}</h1>
  </div>
)

'use client'

import { Navigation } from '@/app/(authenticated)/[tenantId]/components/Navigation'
import { routes } from '@/app/routes'
import { Card, CardContent } from '@/components/ui/card'
import { useWhoAmI } from '@/contexts/WhoAmIContext'
import { FC, useState } from 'react'
import { ApplicableQuota } from '../actions/getApplicableQuotas'
import { Details } from './Details'
import { QuotaUsageCard } from './QuotaUsageCard'
import { QuotaUsageControls } from './QuotaUsageControls'
import { QuotaViewMode } from './quotaUsageConstants'

type QuotaUsageProps = {
  quotas: ApplicableQuota[]
}

export const QuotaUsage: FC<QuotaUsageProps> = ({ quotas }) => {
  const { tenantId } = useWhoAmI()
  const [rangeMinutes, setRangeMinutes] = useState('60')
  const [bucketMinutes, setBucketMinutes] = useState('5')
  const [viewMode, setViewMode] = useState<QuotaViewMode>('requests')

  return (
    <>
      <Navigation href={routes.appRoot()} title="Go back to home" />
      <Details tenantId={tenantId} />
      <hr className="mt-6" />
      {quotas.length === 0 ? (
        <Card className="mt-6">
          <CardContent className="pt-6 text-sm text-muted-foreground">
            No quota applies to you in this tenant, so no usage is being recorded.
          </CardContent>
        </Card>
      ) : (
        <div className="mt-6 space-y-6">
          <QuotaUsageControls
            viewMode={viewMode}
            onViewModeChange={setViewMode}
            bucketMinutes={bucketMinutes}
            onBucketMinutesChange={setBucketMinutes}
            rangeMinutes={rangeMinutes}
            onRangeMinutesChange={setRangeMinutes}
          />
          {quotas.map(applicable => (
            <QuotaUsageCard
              key={applicable.scope}
              {...applicable}
              rangeMinutes={rangeMinutes}
              bucketMinutes={bucketMinutes}
              viewMode={viewMode}
            />
          ))}
        </div>
      )}
    </>
  )
}

import { getUserTaskRun } from '@/app/actions/getUserTaskRun'
import { Metadata } from 'next'
import { notFound } from 'next/navigation'
import { ClientError, Status } from 'nice-grpc-common'
import { Details } from '@/app/(authenticated)/[tenantId]/components/Details'
import LinkWithTenant from '@/app/(authenticated)/[tenantId]/components/LinkWithTenant'
import { AuditTable } from './AuditTable'

import { Button } from '@/components/ui/button'

type Props = { params: { ids: string[]; tenantId: string } }

export default async function Page({ params: { ids, tenantId } }: Props) {
  const [wfRunId, userTaskGuid] = ids

  try {
    const userTaskRun = await getUserTaskRun(tenantId, wfRunId, userTaskGuid)

    return (
      <div>
        <Details
          itemHeader={'UserTaskRun'}
          header={userTaskRun.userTaskDefId?.name ?? 'N/A'}
          version={[userTaskRun.userTaskDefId?.version ?? -1]}
          description={{
            wfRunId: (
              <Button variant="link" className="p-0" asChild>
                <LinkWithTenant href={`/wfRun/${wfRunId}`}>{wfRunId}</LinkWithTenant>
              </Button>
            ),
            userTaskRunId: userTaskGuid,
          }}
        />
        {userTaskRun.events.some(event => event.saved !== undefined) ? (
          <AuditTable events={userTaskRun.events} />
        ) : (
          <div className="mt-20 w-full text-center">
            <p className="text-center text-xl font-bold">No save history found.</p>
            <p className="text-center text-primary/50">This UserTaskRun has not been saved since it was created.</p>
          </div>
        )}
      </div>
    )
  } catch (error) {
    if (error instanceof ClientError && error.code === Status.NOT_FOUND) return notFound()
    throw error
  }
}

export async function generateMetadata({ params: { ids } }: Props): Promise<Metadata> {
  return {
    title: `UserTask Audit ${ids[1]} | Littlehorse`,
  }
}

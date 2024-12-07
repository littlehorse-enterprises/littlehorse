import { getUserTaskRun } from '@/app/actions/getUserTaskRun'
import { Metadata } from 'next'
import { notFound, useParams } from 'next/navigation'
import { ClientError, Status } from 'nice-grpc-common'
import { Table, TableBody, TableCaption, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { Details } from '@/app/[tenantId]/components/Details'
import LinkWithTenant from '@/app/[tenantId]/components/LinkWithTenant'

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
              <LinkWithTenant href={`/wfRun/${wfRunId}`} linkStyle>
                {wfRunId}
              </LinkWithTenant>
            ),
            userTaskRunId: userTaskGuid,
          }}
        />
        <Table>
          <TableCaption>Audit Log</TableCaption>
          <TableHeader>
            <TableRow>
              <TableHead>Timestamp</TableHead>
              {/* <TableHead>User</TableHead> */}
            </TableRow>
          </TableHeader>
          <TableBody>
            {userTaskRun.events.map(event => (
              <TableRow key={event.time}>
                <TableCell className="font-medium">{new Date(event.time as string).toLocaleString()}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
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

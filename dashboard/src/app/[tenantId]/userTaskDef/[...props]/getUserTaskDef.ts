'use server'
import { lhClient } from '@/app/lhClient'
import { WithTenant } from '@/types'

type Props = {
  name: string
  version: string
} & WithTenant

export const getUserTaskDef = async ({ name, version, tenantId }: Props) => {
  const client = await lhClient({ tenantId })

  if (/[0-9]+/.test(version)) {
    return client.getLatestUserTaskDef({ name })
  }

  return client.getUserTaskDef({
    name,
    version: parseInt(version),
  })
}

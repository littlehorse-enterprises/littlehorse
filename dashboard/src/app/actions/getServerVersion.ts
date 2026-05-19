'use server'

import { lhClient } from '@/app/lhClient'
import type { LittleHorseVersion } from 'littlehorse-client/proto'

function formatLittleHorseVersion(littleHorseVersion: LittleHorseVersion): string {
  const parts = [littleHorseVersion.majorVersion, littleHorseVersion.minorVersion, littleHorseVersion.patchVersion]
    .filter(part => part != null)
    .join('.')
  return littleHorseVersion.preReleaseIdentifier ? `${parts}-${littleHorseVersion.preReleaseIdentifier}` : parts
}

export async function getServerVersion(tenantId: string): Promise<string> {
  const client = await lhClient({ tenantId })
  const littleHorseVersion = await client.getServerVersion({})
  return formatLittleHorseVersion(littleHorseVersion)
}

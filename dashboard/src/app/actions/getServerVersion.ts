'use server'

import { lhClient } from '@/app/lhClient'
import type { LittleHorseVersion } from 'littlehorse-client/proto'

function formatLittleHorseVersion(littleHorseVersion: LittleHorseVersion): string {
  const semverCore = `${littleHorseVersion.majorVersion}.${littleHorseVersion.minorVersion}.${littleHorseVersion.patchVersion}`
  return littleHorseVersion.preReleaseIdentifier
    ? `${semverCore}-${littleHorseVersion.preReleaseIdentifier}`
    : semverCore
}

export async function getServerVersion(tenantId: string): Promise<string> {
  const client = await lhClient({ tenantId })
  const littleHorseVersion = await client.getServerVersion({})
  return formatLittleHorseVersion(littleHorseVersion)
}

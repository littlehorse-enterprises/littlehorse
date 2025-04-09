'use client'
import useSWR from 'swr'
import { getWfSpec } from '../(authenticated)/[tenantId]/(diagram)/wfSpec/[...props]/actions/getWfSpec'

export function useWfSpec(tenantId: string, name: string, majorVersion: string, revision: string) {
  const { data, error, isLoading } = useSWR(`wfSpec/${tenantId}/${name}/${majorVersion}/${revision}`, () => {
    return getWfSpec({ tenantId, name, version: `${majorVersion}.${revision}` })
  })

  return {
    wfSpec: data,
    isLoading,
    isError: error,
  }
}

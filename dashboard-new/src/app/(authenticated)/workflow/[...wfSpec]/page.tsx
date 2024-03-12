import { cookies } from 'next/headers'
import { getWfSpec } from './getWfSpec'

export default async function Home({ params: { wfSpec } }: { params: { wfSpec: string[] } }) {
  const name = wfSpec[0]
  const version = wfSpec[1]
  const tenantId = cookies().get('tenantId')!.value
  const spec = await getWfSpec({ tenantId, name, version })

  return <>{spec.createdAt}</>
}

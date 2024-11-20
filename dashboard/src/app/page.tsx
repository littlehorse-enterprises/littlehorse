import { redirect } from 'next/navigation'
import getWhoAmI from './getWhoami'

export default async function Home() {
  if (process.env.LHD_OAUTH_ENABLED === 'true') {
    const whoAmI = await getWhoAmI()
    return redirect(`/${whoAmI.tenants[0]}`)
  }
  return redirect(`/default`)
}

import { redirect } from 'next/navigation'
import { routes } from './routes'
import getWhoAmI from './getWhoami'

export default async function Home() {
  const whoAmI = await getWhoAmI()
  return whoAmI.tenants.length === 0 ? (
    <div className="flex flex-col items-center justify-center">
      <h1 className="text-2xl font-bold">No tenants found for the current principal</h1>
      <p className="mt-3 text-center">
        Try signing into a different principal. <br />
        Please contact your administrator if you think this is a mistake.
      </p>
    </div>
  ) : (
    redirect(routes.tenant.root(whoAmI.tenants[0] ?? 'default'))
  )
}

import { redirect } from 'next/navigation'
import getWhoAmI from './getWhoami'

export default async function Home() {
    const whoAmI = await getWhoAmI()
    return whoAmI.tenants.length === 0 ? (
        <div className="flex flex-col items-center justify-center">
            <h1 className="font-bold text-2xl">No tenants found for the current principal</h1>
            <p className="text-center mt-3">
                Try signing into a different principal. <br />
                Please contact your administrator if you think this is a mistake.
            </p>
        </div>
    ) : (
        redirect(`/${whoAmI.tenants[0] ?? 'default'}`)
    )
}
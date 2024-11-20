import { redirect } from 'next/navigation'
import getWhoAmI from './getWhoami'

export default async function Home() {

    if (process.env.LHD_OAUTH_ENABLED === 'true') {
        const whoAmI = await getWhoAmI()
        if (whoAmI.tenants.length === 0) {
            return <div className="flex flex-col items-center justify-center">
                <h1 className="font-bold text-2xl">No tenants found for the current principal</h1>
                <p className="text-center mt-3">
                    Try signing into a different principal. <br />
                    Please contact your administrator if you think this is a mistake.
                </p>
            </div>
        }
        return redirect(`/${whoAmI.tenants[0]}`)
    }
    return redirect(`/default`)
}

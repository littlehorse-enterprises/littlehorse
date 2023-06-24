import { getProviders } from "next-auth/react"
import { ProviderSigInBtn } from "./ProviderSigInBtn";

async function getData() {
    const providers = await getProviders();
    return providers
}
export async function LoginButtons() {

    const providers = await getData()
    const vproviders = Object.values(providers || {})

  return (
    <>
    {vproviders.map((provider) => <ProviderSigInBtn key={provider.id} provider={provider} num={vproviders.length} />)}
    </>
  )
}

import { LHConfig } from 'littlehorse-client'

const CONFIG = {
  apiHost: process.env.LHC_API_HOST || 'localhost',
  apiPort: process.env.LHC_API_PORT || '2023',
  protocol: process.env.LHC_API_PROTOCOL || 'PLAINTEXT',
  caCert: process.env.LHC_CA_CERT,
}

const config = LHConfig.from(CONFIG)

export const getClient = ({ tenantId, accessToken }: { tenantId?: string; accessToken?: string }) => {
  const config = LHConfig.from({
    ...CONFIG,
    tenantId,
  })

  return config.getClient(accessToken)
}

export default config

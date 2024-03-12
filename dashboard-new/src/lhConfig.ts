import { LHConfig } from 'littlehorse-client'

const config = LHConfig.from({
  apiHost: process.env.LHC_API_HOST || 'localhost',
  apiPort: process.env.LHC_API_PORT || '2023',
  protocol: process.env.LHD_OAUTH_ENABLED === 'true' ? 'SSL' : 'PLAINTEXT',
  caCert: process.env.LHS_CA_CERT,
})

export default config

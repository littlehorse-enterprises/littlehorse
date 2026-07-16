import { LHConfig } from 'littlehorse-client'

const CONFIG = {
  apiHost: process.env.LHC_API_HOST || 'localhost',
  apiPort: process.env.LHC_API_PORT || '2023',
  protocol: process.env.LHC_API_PROTOCOL || 'PLAINTEXT',
  caCert: process.env.LHC_CA_CERT,
  clientCert: process.env.LHC_CLIENT_CERT,
  clientKey: process.env.LHC_CLIENT_KEY,
  grpcMaxReceiveMessageLength: process.env.LHC_GRPC_MAX_RECEIVE_MESSAGE_LENGTH,
}

export const getClient = ({ tenantId, accessToken }: { tenantId?: string; accessToken?: string }) => {
  return LHConfig.from({
    ...CONFIG,
    tenantId,
  }).getClient(accessToken)
}

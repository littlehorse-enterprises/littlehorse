import { LHConfig } from 'littlehorse-client';

const config = LHConfig.from({
    apiHost: process.env.LHC_API_HOST || 'localhost',
    apiPort: process.env.LHC_API_PORT || '2023',
    protocol: process.env.LHC_API_PROTOCOL || 'PLAINTEXT',
    ...(process.env.LHC_CA_CERT ? { caCert: process.env.LHC_CA_CERT } : {})
});

export const getClient = () => config.getClient();

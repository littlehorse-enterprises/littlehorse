import getProperties from './utils/getProperties'

export const CONFIG_NAMES = [
  'LHC_API_HOST',
  'LHC_API_PORT',
  'LHC_API_PROTOCOL',
  'LHW_TASK_WORKER_ID',
  'LHC_TENANT_ID',
  'LHC_CLIENT_CERT',
  'LHC_CLIENT_KEY',
  'LHC_CA_CERT',
  'LHC_OAUTH_CLIENT_ID',
  'LHC_OAUTH_ACCESS_TOKEN_URL',
  'LHW_NUM_WORKER_THREADS',
  'LHW_SERVER_CONNECT_LISTENER',
  'LHC_GRPC_KEEPALIVE_TIME_MS',
  'LHC_GRPC_KEEPALIVE_TIMEOUT_MS',
  'LHW_TASK_WORKER_VERSION',
] as const

export type ConfigName = (typeof CONFIG_NAMES)[number]

export type Config = {
  [key in ConfigName]: string
}

// class LHConfig {
//   private apiHost;
//   private apiPort;
//   private protocol;
//   private tenantId;

//   static fromConfigFile(file: string): LHConfig {
//     const config = await getProperties(file)
//     this.apiHost = config
//   }

//   static with(config: Partial<Config>): LHConfig {

//   }

// }

import { Config, ConfigName } from '../LHConfig'

export type ConfigArgs = {
  apiHost: string
  apiPort: string
  protocol: string
  tenantId: string
  caCert: string
  clientCert: string
  clientKey: string
}

type Mapping = {
  [key in keyof ConfigArgs]: ConfigName
}

const argsMapping: Mapping = {
  apiHost: 'LHC_API_HOST',
  apiPort: 'LHC_API_PORT',
  protocol: 'LHC_API_PROTOCOL',
  tenantId: 'LHC_TENANT_ID',
  caCert: 'LHC_CA_CERT',
  clientCert: 'LHC_CLIENT_CERT',
  clientKey: 'LHC_CLIENT_KEY',
}

/**
 * Maps provided partial ConfigArgs to a partial Config object using the defined
 * environment variable mapping.
 * @param args - Partial set of configuration arguments (ConfigArgs).
 * @returns Partial<Config> object with keys mapped to their corresponding ConfigName.
 */
const getPropertiesArgs = (args: Partial<ConfigArgs>): Partial<Config> => {
  const keys = Object.keys(args) as Array<keyof ConfigArgs>
  return keys.reduce<Partial<Config>>((config, key) => {
    config[argsMapping[key]] = args[key]
    return config
  }, {})
}

export default getPropertiesArgs

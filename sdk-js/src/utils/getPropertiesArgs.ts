import { readFileSync } from 'fs'
import { resolve } from 'path'
import { CONFIG_NAMES, Config, ConfigName } from '../LHConfig'

export type ConfigArgs = {
  apiHost: string
  apiPort: string
  protocol: string
  tenantId: string
  caCert: string
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
}

/**
 * Returns partial object with restricted key/value for valid config properties
 * @param path - absolute path to config file
 * @returns Partial<Config>
 */
const getPropertiesFile = (args: Partial<ConfigArgs>): Partial<Config> => {
  const keys = Object.keys(args) as Array<keyof ConfigArgs>
  return keys.reduce<Partial<Config>>((config, key) => {
    config[argsMapping[key]] = args[key]
    return config
  }, {})
}

export default getPropertiesFile

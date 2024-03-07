import { readFileSync } from 'fs'
import { resolve } from 'path'
import { CONFIG_NAMES, Config } from '../LHConfig'

/**
 * Returns partial object with restricted key/value for valid config properties
 * @param path - absolute path to config file
 * @returns Partial<Config>
 */
const getPropertiesFile = (path: string): Partial<Config> => {
  const fullpath = resolve(path)
  const file = readFileSync(fullpath).toString()
  return file
    .split('\n')
    .filter(Boolean)
    .reduce<Partial<Config>>((config, line) => {
      const match = /^([^#=]+)(={0,1})(.*)$/.exec(line)
      if (match) {
        const property = match[1].trim() as keyof Config
        const value = match[3].trim()
        if (CONFIG_NAMES.includes(property) && !!value) {
          config[property] = value
        }
      }
      return config
    }, {})
}

export default getPropertiesFile

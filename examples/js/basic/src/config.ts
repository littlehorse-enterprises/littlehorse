import { existsSync } from 'fs'
import { homedir } from 'os'
import { join } from 'path'
import { LHConfig } from 'littlehorse-client'

/**
 * House convention (examples/README.md): use ~/.config/littlehorse.config when
 * it exists, else localhost defaults. LHC_* env vars win so harnesses and CI
 * can redirect without touching the file.
 */
export function loadConfig(): LHConfig {
  if (process.env.LHC_API_HOST !== undefined || process.env.LHC_API_PORT !== undefined) {
    return LHConfig.from({})
  }
  const file = join(homedir(), '.config', 'littlehorse.config')
  return existsSync(file) ? LHConfig.fromConfigFile(file) : LHConfig.from({})
}

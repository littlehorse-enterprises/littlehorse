import { LHConfig } from 'littlehorse-client'
import { VariableValue } from 'littlehorse-client/proto'

/**
 * Runs the "example-basic-js" workflow with an optional "input-name" argument.
 *
 * Usage:
 *   npx tsx src/run-wf.ts [name]
 */
async function main() {
  const config = LHConfig.from({})
  const client = config.getClient()

  const name = process.argv[2] ?? 'World'

  const inputVar: VariableValue = { value: { $case: 'str', value: name } }

  console.log(`Running workflow "example-basic-js" with input-name="${name}"...`)

  const result = await client.runWf({
    wfSpecName: 'example-basic-js',
    variables: { 'input-name': inputVar },
  })

  console.log(`Started WfRun: ${result.id?.id}`)
}

main().catch((err) => {
  console.error(err)
  process.exit(1)
})

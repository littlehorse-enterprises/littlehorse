import { LHConfig, toVariableValue } from 'littlehorse-client'

async function main() {
  const config = LHConfig.from({})
  const client = config.getClient()

  const name = process.argv[2] ?? 'Peter'

  const result = await client.runWf({
    wfSpecName: 'example-mutation',
    variables: { name: toVariableValue(name) },
  })

  console.log(`Started WfRun: ${result.id?.id}`)
}

main().catch(err => {
  console.error(err)
  process.exit(1)
})

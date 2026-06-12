import { LHConfig, toVariableValue } from 'littlehorse-client'

async function main() {
  const config = LHConfig.from({})
  const client = config.getClient()

  const t = process.argv[2] ? Number(process.argv[2]) : Date.now()

  const result = await client.runWf({
    wfSpecName: 'example-worker-context',
    variables: { 'request-time': toVariableValue(t) },
  })

  console.log(`Started WfRun: ${result.id?.id}`)
}

main().catch(err => {
  console.error(err)
  process.exit(1)
})

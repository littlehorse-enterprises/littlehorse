import { LHConfig, toVariableValue } from 'littlehorse-client'

async function main() {
  const config = LHConfig.from({})
  const client = config.getClient()

  const raw = process.argv[2]
  const person = raw
    ? (JSON.parse(raw) as Record<string, unknown>)
    : { name: 'Obi-Wan', car: { brand: 'Ford', model: 'Escape' } }

  console.log('Running example-json with person:', person)

  const result = await client.runWf({
    wfSpecName: 'example-json',
    variables: { person: toVariableValue(person) },
  })

  console.log(`Started WfRun: ${result.id?.id}`)
}

main().catch(err => {
  console.error(err)
  process.exit(1)
})
